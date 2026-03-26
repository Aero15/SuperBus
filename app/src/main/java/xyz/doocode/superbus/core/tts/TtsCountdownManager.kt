package xyz.doocode.superbus.core.tts

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import xyz.doocode.superbus.ui.details.components.StopDetailsUtils.parseDurationMinutes
import java.util.Locale

data class TtsSettings(
    val speechRate: Float = 1.0f,
    val pitch: Float = 1.0f,
    val language: Locale = Locale.FRANCE,
    val volume: Float = 1.0f,
    val announceSecondArrival: Boolean = true,
    val announceSecondArrivalOnlyUnder10Min: Boolean = false,
    val askBeforeExit: Boolean = true
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

    private val _activeSubscriptions =
        MutableStateFlow<Map<String, CountdownSubscription>>(emptyMap())
    val activeSubscriptions: StateFlow<Map<String, CountdownSubscription>> =
        _activeSubscriptions.asStateFlow()

    private val _isTtsReady = MutableStateFlow(false)
    val isTtsReady: StateFlow<Boolean> = _isTtsReady.asStateFlow()

    private var lastGroupedArrivals: Map<String, List<xyz.doocode.superbus.core.dto.Temps>> =
        emptyMap()

    private var settings = loadSettings()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var announcementJob: Job? = null

    private val prefs by lazy {
        appContext.getSharedPreferences("superbus_tts_settings", Context.MODE_PRIVATE)
    }

    fun init() {
        if (tts != null) return
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
        )
    }

    fun saveSettings(newSettings: TtsSettings) {
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
            .apply()
        applySettings()
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
    }

    fun isSubscribed(key: String): Boolean = _activeSubscriptions.value.containsKey(key)

    fun hasActiveSubscriptions(): Boolean = _activeSubscriptions.value.isNotEmpty()

    fun clearAllSubscriptions() {
        _activeSubscriptions.value = emptyMap()
        tts?.stop()
    }

    /**
     * Called by the ViewModel when arrival data is refreshed.
     * Checks each subscription and announces if the minute count changed.
     */
    fun onArrivalsUpdated(groupedArrivals: Map<String, List<xyz.doocode.superbus.core.dto.Temps>>) {
        lastGroupedArrivals = groupedArrivals
        internalProcessArrivals(groupedArrivals)
    }

    private fun internalProcessArrivals(groupedArrivals: Map<String, List<xyz.doocode.superbus.core.dto.Temps>>) {
        if (!ttsReady || _activeSubscriptions.value.isEmpty()) return

        val subscriptions = _activeSubscriptions.value.toMap()
        val hasMultipleSubscriptions = subscriptions.size > 1
        val announcements = mutableListOf<String>()
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

            announcements.add(text)

            // Remove subscription when countdown reaches 0
            if (minutes <= 0) {
                keysToRemove.add(key)
            }
        }

        if (keysToRemove.isNotEmpty()) {
            val currentMap = _activeSubscriptions.value.toMutableMap()
            keysToRemove.forEach { currentMap.remove(it) }
            _activeSubscriptions.value = currentMap
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
                minutes > 1 -> "Dans $minutes minutes, ligne $numLigne, direction $destination"
                minutes == 1 -> "En approche, ligne $numLigne, direction $destination"
                else -> "Départ imminent, ligne $numLigne, direction $destination"
            }

            val nextPart = if (nextMinutes != null) {
                when {
                    nextMinutes > 1 -> ", le suivant dans $nextMinutes minutes"
                    nextMinutes == 1 -> ", le suivant en approche"
                    else -> ", le suivant est imminent"
                }
            } else ""

            firstPart + nextPart
        } else {
            val firstPart = when {
                minutes > 1 -> if (nextMinutes != null) "Prochain passage dans $minutes minutes" else "Dans $minutes minutes"
                minutes == 1 -> "En approche"
                else -> "Départ imminent"
            }

            val nextPart = if (nextMinutes != null) {
                when {
                    nextMinutes > 1 -> ", le suivant dans $nextMinutes minutes"
                    nextMinutes == 1 -> ", le suivant en approche"
                    else -> ", le suivant est imminent"
                }
            } else ""

            firstPart + nextPart
        }
    }

    private fun queueAnnouncements(announcements: List<String>) {
        announcementJob?.cancel()
        announcementJob = scope.launch {
            for ((index, text) in announcements.withIndex()) {
                if (index > 0) {
                    delay(500) // Pause between announcements
                }
                speak(text)
                awaitSpeechDone()
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
