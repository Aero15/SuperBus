package xyz.doocode.superbus.core.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import xyz.doocode.superbus.core.dto.ginko.FavoriteStation
import xyz.doocode.superbus.core.dto.ginko.Ligne
import androidx.core.content.edit

class FavoritesRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("superbus_favorites", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val _favorites = MutableStateFlow<List<FavoriteStation>>(loadFavorites())
    val favorites: StateFlow<List<FavoriteStation>> = _favorites.asStateFlow()

    private fun loadFavorites(): List<FavoriteStation> {
        val json = prefs.getString("favorites_list", null) ?: return emptyList()
        val type = object : TypeToken<List<FavoriteStation>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun saveFavorites(list: List<FavoriteStation>) {
        val json = gson.toJson(list)
        prefs.edit { putString("favorites_list", json) }
        _favorites.value = list
    }

    fun updateFavoritesOrder(newOrder: List<FavoriteStation>) {
        saveFavorites(newOrder)
    }

    fun isFavorite(stopId: String, detailsFromId: Boolean): Boolean {
        return _favorites.value.any { it.id == stopId && it.detailsFromId == detailsFromId }
    }

    fun addFavorite(
        stopId: String,
        stopName: String,
        detailsFromId: Boolean,
        lines: List<Ligne>
    ) {
        val currentList = _favorites.value.toMutableList()
        val filteredLines = lines.filter { it.typologie <= 30 }
        if (currentList.none { it.id == stopId && it.detailsFromId == detailsFromId }) {
            currentList.add(
                FavoriteStation(
                    id = stopId,
                    detailsFromId = detailsFromId,
                    name = stopName,
                    lines = filteredLines,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )
            saveFavorites(currentList)
        }
    }


    fun renameFavorite(stopId: String, detailsFromId: Boolean, newName: String) {
        val currentList = _favorites.value.toMutableList()
        val index =
            currentList.indexOfFirst { it.id == stopId && it.detailsFromId == detailsFromId }

        if (index != -1) {
            val oldFav = currentList[index]
            if (oldFav.name != newName) {
                val newFav = oldFav.copy(
                    name = newName,
                    updatedAt = System.currentTimeMillis()
                )
                currentList[index] = newFav
                saveFavorites(currentList)
            }
        }
    }

    fun removeFavorite(stopId: String, detailsFromId: Boolean) {
        val currentList = _favorites.value.toMutableList()
        if (currentList.removeIf { it.id == stopId && it.detailsFromId == detailsFromId }) {
            saveFavorites(currentList)
        }
    }

    fun updateFavoriteLines(stopId: String, detailsFromId: Boolean, lines: List<Ligne>) {
        val favoriteList = _favorites.value.toMutableList()
        val index =
            favoriteList.indexOfFirst { it.id == stopId && it.detailsFromId == detailsFromId }

        if (index != -1) {
            val oldFav = favoriteList[index]
            val filteredLines = lines.filter { it.typologie <= 30 && it.id !in listOf("110") }

            if (oldFav.lines != filteredLines) {
                val newFav = oldFav.copy(
                    lines = filteredLines,
                    updatedAt = System.currentTimeMillis()
                )
                favoriteList[index] = newFav
                saveFavorites(favoriteList)
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: FavoritesRepository? = null

        fun getInstance(context: Context): FavoritesRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FavoritesRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
