package com.am.drinks.drinks

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

// Class responsible for saving and retrieving persistent drink data using SharedPreferences.
class DrinkPreferences(context: Context) {

    companion object {
        private const val PREF_NAME = "drink_prefs"
        private const val RECENT_DRINKS_KEY = "recently_viewed"
        private const val FAVOURITES_KEY = "favourites"
        private const val MAX_RECENT = 10
    }

    // SharedPreferences to store user's preferences
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    // <reified T> – type T is known during working time, needed for Gson with List<T>
    private inline fun <reified T> readListFromPrefs(key: String): List<T> {
        val json = prefs.getString(key, null)
        return if (json != null) {
            try {
                val type = object : TypeToken<List<T>>() {}.type
                gson.fromJson(json, type)
            } catch (_: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    // Recently viewed, flow giving list of drinks, emit = return
    fun getRecentlyViewed(): Flow<List<Drink>> = flow {
        emit(readListFromPrefs(RECENT_DRINKS_KEY))
    }

    fun saveRecentlyViewed(drinks: List<Drink>) {
        val json = gson.toJson(drinks.take(MAX_RECENT))
        prefs.edit().putString(RECENT_DRINKS_KEY, json).apply()
    }

    // Favourites
    fun getFavourites(): Flow<List<Drink>> = flow {
        emit(readListFromPrefs(FAVOURITES_KEY))
    }

    fun saveFavourites(drinks: List<Drink>) {
        val json = gson.toJson(drinks)
        prefs.edit().putString(FAVOURITES_KEY, json).apply()
    }
}
