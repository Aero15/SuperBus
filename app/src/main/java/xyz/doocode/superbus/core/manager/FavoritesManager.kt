package xyz.doocode.superbus.core.manager

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xyz.doocode.superbus.core.api.ApiClient
import xyz.doocode.superbus.core.data.FavoritesRepository
import xyz.doocode.superbus.core.dto.Ligne

class FavoritesManager(context: Context) {

    private val repository = FavoritesRepository.getInstance(context)

    suspend fun toggleFavorite(stopId: String, stopName: String) {
        if (repository.isFavorite(stopId)) {
            repository.removeFavorite(stopId)
        } else {
            addFavorite(stopId, stopName)
        }
    }

    private suspend fun addFavorite(stopId: String, stopName: String) {
        try {
            val lines = fetchLines(stopId)
            repository.addFavorite(stopId, stopName, lines)
        } catch (e: Exception) {
            // Fallback: add with empty lines if network fails
            repository.addFavorite(stopId, stopName, emptyList())
        }
    }

    suspend fun refreshFavoriteLines(stopId: String) {
        if (!repository.isFavorite(stopId)) return

        try {
            val lines = fetchLines(stopId)
            repository.updateFavoriteLines(stopId, lines)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun fetchLines(stopId: String): List<Ligne> = withContext(Dispatchers.IO) {
        val response = ApiClient.ginkoService.getVariantesDesservantArret(stopId)
        response.objects
    }
}
