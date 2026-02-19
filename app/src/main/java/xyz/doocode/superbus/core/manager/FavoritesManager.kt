package xyz.doocode.superbus.core.manager

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xyz.doocode.superbus.core.api.ApiClient
import xyz.doocode.superbus.core.data.FavoritesRepository
import xyz.doocode.superbus.core.dto.Ligne

class FavoritesManager(context: Context) {

    private val repository = FavoritesRepository.getInstance(context)

    fun isFavorite(stopId: String): Boolean {
        return repository.isFavorite(stopId)
    }

    suspend fun toggleFavorite(stopId: String, groupedIds: List<String>, stopName: String) {
        if (repository.isFavorite(stopId)) {
            repository.removeFavorite(stopId)
        } else {
            addFavorite(stopId, groupedIds, stopName)
        }
    }

    private suspend fun addFavorite(stopId: String, groupedIds: List<String>, stopName: String) {
        try {
            val lines = try {
                fetchLines(stopId)
            } catch (e: Exception) {
                // Try fetching lines with other grouped IDs if main one fails
                var successLines: List<Ligne> = emptyList()
                for (otherId in groupedIds) {
                    if (otherId == stopId) continue
                    try {
                        successLines = fetchLines(otherId)
                        if (successLines.isNotEmpty()) break
                    } catch (_: Exception) {
                    }
                }
                successLines
            }
            repository.addFavorite(stopId, groupedIds, stopName, lines)
        } catch (e: Exception) {
            // Fallback
            repository.addFavorite(stopId, groupedIds, stopName, emptyList())
        }
    }

    fun updateFavoriteGroupedIds(stopId: String, groupedIds: List<String>) {
        if (repository.isFavorite(stopId)) {
            repository.updateFavoriteGroupedIds(stopId, groupedIds)
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
