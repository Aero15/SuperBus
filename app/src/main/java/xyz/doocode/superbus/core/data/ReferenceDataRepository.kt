package xyz.doocode.superbus.core.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import xyz.doocode.superbus.core.api.ApiClient
import xyz.doocode.superbus.core.dto.Arret
import xyz.doocode.superbus.core.dto.Ligne

class ReferenceDataRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("superbus_cache", Context.MODE_PRIVATE)
    private val gson = Gson()

    // 24 hours in milliseconds
    private val CACHE_VALIDITY_DURATION = 24 * 60 * 60 * 1000L // 24 hours

    companion object {
        private const val KEY_ARRETS_DATA = "cache_arrets_data"
        private const val KEY_ARRETS_TIMESTAMP = "cache_arrets_timestamp"

        private const val KEY_LIGNES_DATA = "cache_lignes_data"
        private const val KEY_LIGNES_TIMESTAMP = "cache_lignes_timestamp"

        @Volatile // Ensure that changes to this variable are visible to all threads.
        private var instance: ReferenceDataRepository? = null

        fun getInstance(context: Context): ReferenceDataRepository {
            return instance
                ?: synchronized(this) { // synchronized ensures that only one thread can run this block at a time.
                    instance ?: ReferenceDataRepository(context.applicationContext).also {
                        instance = it
                    } // Create the instance if it doesn't exist, and assign it to the variable.
                }
        }
    }

    suspend fun getDetailsVariante(
        idLigne: String,
        idVariante: String,
        forceRefresh: Boolean = false
    ): List<Arret> {
        val cacheKeyData = "cache_variante_${idLigne}_${idVariante}_data"
        val cacheKeyTimestamp = "cache_variante_${idLigne}_${idVariante}_timestamp"

        return getData(
            cacheKeyData = cacheKeyData,
            cacheKeyTimestamp = cacheKeyTimestamp,
            typeToken = object : TypeToken<List<Arret>>() {}.type,
            forceRefresh = forceRefresh
        ) {
            ApiClient.ginkoService.getDetailsVariante(idLigne, idVariante).objects
        }
    }

    suspend fun getArrets(forceRefresh: Boolean = false): List<Arret> {
        return getData(
            cacheKeyData = KEY_ARRETS_DATA,
            cacheKeyTimestamp = KEY_ARRETS_TIMESTAMP,
            typeToken = object : TypeToken<List<Arret>>() {}.type,
            forceRefresh = forceRefresh
        ) {
            ApiClient.ginkoService.getArrets().objects
        }
    }

    suspend fun getLignes(forceRefresh: Boolean = false): List<Ligne> {
        return getData(
            cacheKeyData = KEY_LIGNES_DATA,
            cacheKeyTimestamp = KEY_LIGNES_TIMESTAMP,
            typeToken = object : TypeToken<List<Ligne>>() {}.type,
            forceRefresh = forceRefresh
        ) {
            ApiClient.ginkoService.getLignes().objects
        }
    }

    private suspend fun <T> getData(
        cacheKeyData: String,
        cacheKeyTimestamp: String,
        typeToken: java.lang.reflect.Type,
        forceRefresh: Boolean,
        apiCall: suspend () -> List<T>
    ): List<T> {
        // Check cache validity and return cached data if valid
        if (!forceRefresh && isCacheValid(cacheKeyTimestamp)) {
            val json = prefs.getString(cacheKeyData, null)
            if (json != null) { // If cache exists, try to parse it
                // Try to parse it
                try {
                    return gson.fromJson(json, typeToken)
                } catch (e: Exception) {
                    // Falls through to API call if cache is corrupted
                    e.printStackTrace()
                }
            }
        }

        // Fetch data from API and cache it
        val data = apiCall()
        val json = gson.toJson(data)
        prefs.edit()
            .putString(cacheKeyData, json)
            .putLong(cacheKeyTimestamp, System.currentTimeMillis())
            .apply()

        return data
    }

    private fun isCacheValid(timestampKey: String): Boolean {
        val lastUpdate = prefs.getLong(timestampKey, 0)
        return (System.currentTimeMillis() - lastUpdate) < CACHE_VALIDITY_DURATION
    }
}
