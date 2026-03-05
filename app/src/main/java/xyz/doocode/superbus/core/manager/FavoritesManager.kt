package xyz.doocode.superbus.core.manager

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xyz.doocode.superbus.core.api.ApiClient
import xyz.doocode.superbus.core.data.FavoritesRepository
import xyz.doocode.superbus.core.data.ReferenceDataRepository
import xyz.doocode.superbus.core.dto.Ligne

class FavoritesManager(context: Context) {

    private val repository = FavoritesRepository.getInstance(context)
    private val referenceDataRepository = ReferenceDataRepository.getInstance(context)

    fun isFavorite(stopId: String, detailsFromId: Boolean): Boolean {
        return repository.isFavorite(stopId, detailsFromId)
    }

    suspend fun toggleFavorite(stopId: String, stopName: String, detailsFromId: Boolean) {
        if (repository.isFavorite(stopId, detailsFromId)) {
            repository.removeFavorite(stopId, detailsFromId)
        } else {
            addFavorite(stopId, stopName, detailsFromId)
        }
    }

    private suspend fun addFavorite(stopId: String, stopName: String, detailsFromId: Boolean) {
        try {
            val lines = try {
                fetchLines(stopId)
            } catch (e: Exception) {
                emptyList()
            }
            repository.addFavorite(stopId, stopName, detailsFromId, lines)
        } catch (e: Exception) {
            // Fallback
            repository.addFavorite(stopId, stopName, detailsFromId, emptyList())
        }
    }


    suspend fun refreshFavoriteLines(stopId: String, detailsFromId: Boolean) {
        if (!repository.isFavorite(stopId, detailsFromId)) return

        try {
            val lines = if (detailsFromId) {
                fetchLines(stopId)
            } else {
                fetchLinesByName(stopId)
            }
            repository.updateFavoriteLines(stopId, detailsFromId, lines)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun fetchLinesByName(stopId: String): List<Ligne> =
        withContext(Dispatchers.IO) {
            val stopDetails = ApiClient.ginkoService.getDetailsArret(stopId).objects
            val stopName = stopDetails.nom

            val tempsLieu = ApiClient.ginkoService.getTempsLieu(nom = stopName).objects
            val activeLineIds = tempsLieu.listeTemps.map { it.idLigne }.toSet()

            val allLines = referenceDataRepository.getLignes()
            allLines.filter { it.id in activeLineIds }
        }

    private suspend fun fetchLines(stopId: String): List<Ligne> = withContext(Dispatchers.IO) {
        val response = ApiClient.ginkoService.getVariantesDesservantArret(stopId)
        response.objects
    }
}
