package xyz.doocode.superbus.core.tts

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.os.PowerManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import xyz.doocode.superbus.MainActivity
import xyz.doocode.superbus.R
import xyz.doocode.superbus.core.dto.ginko.Temps
import java.util.Locale

data class TtsSettings(
    val speechRate: Float = 1.0f,
    val pitch: Float = 1.0f,
    val language: Locale = Locale.FRANCE,
    val volume: Float = 1.0f,
    val announceSecondArrival: Boolean = true,
    val announceSecondArrivalOnlyUnder10Min: Boolean = false,
    val askBeforeExit: Boolean = true,
    val removeSubOnZero: Boolean = true,
    val allowBackground: Boolean = false
)

data class CountdownSubscription(
    val key: String, // "numLigne|destination"
    val numLigne: String,
    val destination: String,
    var lastAnnouncedText: String = "",
    var lastAnnouncedMinutes: Int = -1
)

class TtsCountdownManager(context: Context) {

    private val appContext = context.applicationContext
    private var tts: TextToSpeech? = null
    private var ttsReady = false

    private val notificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val powerManager = appContext.getSystemService(Context.POWER_SERVICE) as PowerManager
    private var wakeLock: PowerManager.WakeLock? = null
    private val CHANNEL_ID = "tts_countdown_channel"
    private val NOTIFICATION_ID = 42

    private val _activeSubscriptions =
        MutableStateFlow<Map<String, CountdownSubscription>>(emptyMap())
    val activeSubscriptions: StateFlow<Map<String, CountdownSubscription>> =
        _activeSubscriptions.asStateFlow()

    private val _isTtsReady = MutableStateFlow(false)
    val isTtsReady: StateFlow<Boolean> = _isTtsReady.asStateFlow()

    private val _currentlySpeakingKey = MutableStateFlow<String?>(null)
    val currentlySpeakingKey: StateFlow<String?> = _currentlySpeakingKey.asStateFlow()

    private var lastGroupedArrivals: Map<String, List<Temps>> =
        emptyMap()

    private var settings = loadSettings()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var announcementJob: Job? = null
    private var notificationTickerJob: Job? = null

    private val prefs by lazy {
        appContext.getSharedPreferences("superbus_tts_settings", Context.MODE_PRIVATE)
    }

    fun init() {
        if (tts != null) return
        createNotificationChannel()
        tts = TextToSpeech(appContext) { status ->
            ttsReady = status == TextToSpeech.SUCCESS
            _isTtsReady.value = ttsReady
            if (ttsReady) {
                applySettings()
            }
        }
    }

    private fun loadSettings(): TtsSettings {
        val prefs = appContext.getSharedPreferences("superbus_tts_settings", Context.MODE_PRIVATE)
        return TtsSettings(
            speechRate = prefs.getFloat("speech_rate", 1.0f),
            pitch = prefs.getFloat("pitch", 1.0f),
            language = Locale.Builder()
                .setLanguage(
                    prefs.getString("language", Locale.FRANCE.language) ?: Locale.FRANCE.language
                )
                .setRegion(
                    prefs.getString("country", Locale.FRANCE.country) ?: Locale.FRANCE.country
                )
                .build(),
            volume = prefs.getFloat("volume", 1.0f),
            announceSecondArrival = prefs.getBoolean("announce_second_arrival", true),
            announceSecondArrivalOnlyUnder10Min = prefs.getBoolean(
                "announce_second_arrival_only_under_10",
                false
            ),
            askBeforeExit = prefs.getBoolean("ask_before_exit", true),
            removeSubOnZero = prefs.getBoolean("remove_sub_on_zero", true),
            allowBackground = prefs.getBoolean("allow_background", false),
        )
    }

    fun saveSettings(newSettings: TtsSettings) {
        val oldAllowBackground = settings.allowBackground
        settings = newSettings
        prefs.edit()
            .putFloat("speech_rate", newSettings.speechRate)
            .putFloat("pitch", newSettings.pitch)
            .putString("language", newSettings.language.language)
            .putString("country", newSettings.language.country)
            .putFloat("volume", newSettings.volume)
            .putBoolean("announce_second_arrival", newSettings.announceSecondArrival)
            .putBoolean(
                "announce_second_arrival_only_under_10",
                newSettings.announceSecondArrivalOnlyUnder10Min
            )
            .putBoolean("ask_before_exit", newSettings.askBeforeExit)
            .putBoolean("remove_sub_on_zero", newSettings.removeSubOnZero)
            .putBoolean("allow_background", newSettings.allowBackground)
            .apply()
        applySettings()

        if (oldAllowBackground && !newSettings.allowBackground) {
            notificationManager.cancel(NOTIFICATION_ID)
        } else if (!oldAllowBackground && newSettings.allowBackground && lastGroupedArrivals.isNotEmpty()) {
            updateNotification(lastGroupedArrivals)
        }
    }

    fun getSettings(): TtsSettings = settings

    private fun applySettings() {
        tts?.let { engine ->
            engine.language = settings.language
            engine.setSpeechRate(settings.speechRate)
            engine.setPitch(settings.pitch)
        }
    }

    fun toggleSubscription(key: String, numLigne: String, destination: String) {
        val current = _activeSubscriptions.value.toMutableMap()
        val isAdded = !current.containsKey(key)
        if (current.containsKey(key)) {
            current.remove(key)
        } else {
            current[key] = CountdownSubscription(key, numLigne, destination)
        }
        _activeSubscriptions.value = current

        if (isAdded && lastGroupedArrivals.isNotEmpty()) {
            internalProcessArrivals(lastGroupedArrivals)
        }
        updateNotification(lastGroupedArrivals)
    }

    fun isSubscribed(key: String): Boolean = _activeSubscriptions.value.containsKey(key)

    fun hasActiveSubscriptions(): Boolean = _activeSubscriptions.value.isNotEmpty()

    fun clearAllSubscriptions() {
        _activeSubscriptions.value = emptyMap()
        tts?.stop()
        TtsForegroundService.stop(appContext)
    }

    private fun createNotificationChannel() {
        val name = "Suivi des départs"
        val descriptionText = "Affiche le compte à rebours de l'arrivée des bus en temps réel"
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun startNotificationTicker() {
        if (notificationTickerJob?.isActive == true) return
        notificationTickerJob = scope.launch {
            while (true) {
                delay(15000) // Update every 15s if no data update happened
                if (_activeSubscriptions.value.isNotEmpty() && lastGroupedArrivals.isNotEmpty()) {
                    updateNotification(lastGroupedArrivals)
                }
            }
        }
    }

    private fun stopNotificationTicker() {
        notificationTickerJob?.cancel()
        notificationTickerJob = null
    }

    private fun updateNotification(groupedArrivals: Map<String, List<Temps>>) {
        val subs = _activeSubscriptions.value
        if (subs.isEmpty() || !settings.allowBackground) {
            stopNotificationTicker()
            TtsForegroundService.stop(appContext)
            return
        }

        startNotificationTicker()
        val lines = mutableListOf<String>()
        for ((key, sub) in subs) {
            val arrivals = groupedArrivals[key] ?: continue
            val firstArrival = arrivals.firstOrNull() ?: continue
            val minutes = parseDurationMinutes(firstArrival.temps, firstArrival.tempsEnSeconde)
            val timeText = if (minutes > 0) "$minutes minutes" else "départ imminent"
            lines.add("Dans $timeText, [${sub.numLigne}] ${sub.destination}")
        }

        if (lines.isEmpty()) {
            TtsForegroundService.stop(appContext)
            return
        }

        val contentText = if (lines.size == 1) lines[0] else "${lines.size} lignes suivies"
        val bigText = lines.joinToString("\n")

        TtsForegroundService.start(appContext, "Suivi SuperBus", contentText, bigText)
    }

    /**
     * Called by the ViewModel when arrival data is refreshed.
     * Checks each subscription and announces if the minute count changed.
     */
    fun onArrivalsUpdated(groupedArrivals: Map<String, List<Temps>>) {
        lastGroupedArrivals = groupedArrivals
        internalProcessArrivals(groupedArrivals)
        updateNotification(groupedArrivals)
    }

    private fun internalProcessArrivals(groupedArrivals: Map<String, List<Temps>>) {
        if (!ttsReady || _activeSubscriptions.value.isEmpty()) return

        val subscriptions = _activeSubscriptions.value.toMap()
        val hasMultipleSubscriptions = subscriptions.size > 1
        val announcements = mutableListOf<Pair<String, String>>()
        val keysToRemove = mutableListOf<String>()

        for ((key, sub) in subscriptions) {
            val arrivals = groupedArrivals[key] ?: continue
            val firstArrival = arrivals.firstOrNull() ?: continue
            val minutes = parseDurationMinutes(firstArrival.temps, firstArrival.tempsEnSeconde)

            val shouldAnnounceSecond = settings.announceSecondArrival &&
                    (!settings.announceSecondArrivalOnlyUnder10Min || minutes <= 10)

            val secondArrival = if (shouldAnnounceSecond) arrivals.getOrNull(1) else null
            val nextMinutes =
                secondArrival?.let { parseDurationMinutes(it.temps, it.tempsEnSeconde) }

            val text = buildAnnouncementText(
                sub.numLigne,
                sub.destination,
                minutes,
                nextMinutes,
                hasMultipleSubscriptions
            )

            // Only announce if the exact phrase hasn't been spoken yet AND the first arrival minutes have changed
            // This prevents re-announcing just because the 2nd arrival time changed.
            if (minutes == sub.lastAnnouncedMinutes || text == sub.lastAnnouncedText) continue

            // Update last announced text and minutes
            val updated = sub.copy(lastAnnouncedText = text, lastAnnouncedMinutes = minutes)
            val currentMap = _activeSubscriptions.value.toMutableMap()
            currentMap[key] = updated
            _activeSubscriptions.value = currentMap

            announcements.add(key to text)

            // Remove subscription when countdown reaches 0 (if setting is enabled)
            if (settings.removeSubOnZero && minutes <= 0) {
                keysToRemove.add(key)
            }
        }

        if (keysToRemove.isNotEmpty()) {
            val currentMap = _activeSubscriptions.value.toMutableMap()
            keysToRemove.forEach { currentMap.remove(it) }
            _activeSubscriptions.value = currentMap
            updateNotification(groupedArrivals)
        }

        if (announcements.isNotEmpty()) {
            queueAnnouncements(announcements)
        }
    }

    private fun parseDurationMinutes(timeStr: String, tempsEnSeconde: Int): Int {
        return if (timeStr.contains("min")) {
            timeStr.filter { it.isDigit() }.toIntOrNull() ?: (tempsEnSeconde / 60)
        } else if (tempsEnSeconde > 0) {
            tempsEnSeconde / 60
        } else if (timeStr.contains("h") && !timeStr.contains(":")) {
            try {
                val parts = timeStr.lowercase().split("h")
                val h = parts[0].trim().filter { it.isDigit() }.toInt()
                val m = parts.getOrNull(1)?.trim()?.filter { it.isDigit() }?.toIntOrNull() ?: 0
                h * 60 + m
            } catch (e: Exception) {
                tempsEnSeconde / 60
            }
        } else {
            tempsEnSeconde / 60
        }
    }

    private fun buildAnnouncementText(
        numLigne: String,
        destination: String,
        minutes: Int,
        nextMinutes: Int?,
        hasMultiple: Boolean
    ): String {
        return if (hasMultiple) {
            val firstPart = when {
                minutes > 1 -> "Départ, dans $minutes minutes, pour la ligne $numLigne, direction $destination"
                minutes == 1 -> "Arrivée imminente, de la ligne $numLigne, direction $destination"
                else -> "Départ imminent, de la ligne $numLigne, direction $destination"
            }

            val nextPart = if (nextMinutes != null) {
                ", le prochain, dans $nextMinutes minutes"
            } else ""

            firstPart + nextPart
        } else {
            val firstPart = when {
                minutes > 1 -> "Départ, dans $minutes minutes"
                minutes == 1 -> "Arrivée imminente"
                else -> "Départ imminent"
            }

            val nextPart = if (nextMinutes != null) {
                ", le prochain, dans $nextMinutes minutes"
            } else ""

            firstPart + nextPart
        }
    }

    private fun queueAnnouncements(announcements: List<Pair<String, String>>) {
        announcementJob?.cancel()
        announcementJob = scope.launch {
            for ((index, item) in announcements.withIndex()) {
                val (key, text) = item
                if (index > 0) {
                    delay(500) // Pause between announcements
                }
                _currentlySpeakingKey.value = key
                speak(text)
                awaitSpeechDone()
                _currentlySpeakingKey.value = null
            }
        }
    }

    fun speakNow(text: String) {
        if (!ttsReady) return
        tts?.stop()
        speak(text)
    }

    private fun speak(text: String, temporarySettings: TtsSettings? = null) {
        val s = temporarySettings ?: settings
        tts?.language = s.language
        tts?.setSpeechRate(s.speechRate)
        tts?.setPitch(s.pitch)

        if (settings.allowBackground && wakeLock == null) {
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "SuperBus:TtsWakeLock"
            ).apply { acquire(10 * 60 * 1000L /*10 minutes max*/) }
        }

        val params = Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, s.volume)
        }
        tts?.speak(text, TextToSpeech.QUEUE_ADD, params, "superbus_${System.nanoTime()}")
    }

    fun testTTS(testSettings: TtsSettings) {
        if (!ttsReady) return
        tts?.stop()
        speak("Ceci est un test de la synthèse vocale", testSettings)
    }

    private suspend fun awaitSpeechDone() {
        if (tts?.isSpeaking != true) return
        suspendCancellableCoroutine { cont ->
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) {
                    if (cont.isActive) cont.resume(Unit) {}
                    if (wakeLock?.isHeld == true) {
                        wakeLock?.release()
                        wakeLock = null
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    if (cont.isActive) cont.resume(Unit) {}
                }
            })
        }
    }

    fun announcePause() {
        if (!ttsReady || _activeSubscriptions.value.isEmpty()) return
        speakNow("Annonces suspendues")
    }

    fun shutdown() {
        announcementJob?.cancel()
        scope.cancel()
        tts?.stop()
        tts?.shutdown()
        tts = null
        ttsReady = false
        _isTtsReady.value = false
    }
}
