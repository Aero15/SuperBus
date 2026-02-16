package xyz.doocode.superbus.core.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import xyz.doocode.superbus.core.dto.FavoriteStation
import xyz.doocode.superbus.core.dto.Ligne

class FavoritesRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("superbus_favorites", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val _favorites = MutableStateFlow<List<FavoriteStation>>(loadFavorites())
    val favorites: StateFlow<List<FavoriteStation>> = _favorites.asStateFlow()

    private fun loadFavorites(): List<FavoriteStation> {
        val json = prefs.getString("favorites_list", null) ?: return emptyList()
        val type = object : TypeToken<List<FavoriteStation>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveFavorites(list: List<FavoriteStation>) {
        val json = gson.toJson(list)
        prefs.edit().putString("favorites_list", json).apply()
        _favorites.value = list
    }

    fun isFavorite(stopId: String): Boolean {
        return _favorites.value.any { it.id == stopId }
    }

    fun addFavorite(stopId: String, stopName: String, lines: List<Ligne>) {
        val currentList = _favorites.value.toMutableList()
        if (currentList.none { it.id == stopId }) {
            currentList.add(
                FavoriteStation(
                    id = stopId,
                    name = stopName,
                    lines = lines,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )
            saveFavorites(currentList)
        }
    }

    fun removeFavorite(stopId: String) {
        val currentList = _favorites.value.toMutableList()
        if (currentList.removeIf { it.id == stopId }) {
            saveFavorites(currentList)
        }
    }

    fun updateFavoriteLines(stopId: String, lines: List<Ligne>) {
        val currentList = _favorites.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == stopId }

        if (index != -1) {
            val oldFav = currentList[index]
            
            if (oldFav.lines != lines) {
                val newFav = oldFav.copy(
                    lines = lines,
                    updatedAt = System.currentTimeMillis()
                )
                currentList[index] = newFav
                saveFavorites(currentList)
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
