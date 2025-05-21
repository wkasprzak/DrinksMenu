package com.am.drinks.drinks

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DrinkViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val MAX_RECENT_VIEWED = 10
    }

    // Helper for reading/writing favourites and recently viewed to SharedPreferences
    private val drinkPrefs = DrinkPreferences(application)

    private val _drinks = MutableStateFlow<List<Drink>>(emptyList())
    val drinks: StateFlow<List<Drink>> = _drinks.asStateFlow()

    private val _isLoading = MutableStateFlow(true)

    private val _favourites = MutableStateFlow<List<Drink>>(emptyList())
    val favourites: StateFlow<List<Drink>> = _favourites.asStateFlow()

    private val _hasError = MutableStateFlow(false)
    val hasError: StateFlow<Boolean> = _hasError.asStateFlow()

    private val _recentlyViewed = MutableStateFlow<List<Drink>>(emptyList())
    val recentlyViewed: StateFlow<List<Drink>> = _recentlyViewed.asStateFlow()

    // Loading drinks from API, recently views and favourites from local storage
    init {
        loadDrinks()
        collectTo(drinkPrefs.getRecentlyViewed(), _recentlyViewed)
        collectTo(drinkPrefs.getFavourites(), _favourites)
    }

    private fun <T> collectTo(flow: Flow<T>, target: MutableStateFlow<T>) {
        viewModelScope.launch {
            flow
                .catch { e -> Log.e("DrinkViewModel", "Flow error: ${e.message}", e) }
                .collect { target.value = it }
        }
    }

    fun refreshFavourites() {
        viewModelScope.launch {
            _favourites.value = loadFavouritesFromStorage()
        }
    }

    private suspend fun loadFavouritesFromStorage(): List<Drink> {
        return drinkPrefs.getFavourites().first()
    }

    fun isFavourite(drinkId: String): Boolean {
        return _favourites.value.any { it.id == drinkId }
    }

    fun toggleFavourite(drink: Drink) {
        viewModelScope.launch {val isFav = isFavourite(drink.id)
            val newList = if (isFav) {
                _favourites.value.filterNot { it.id == drink.id }
            } else {
                _favourites.value + drink
            }
            _favourites.value = newList
            drinkPrefs.saveFavourites(newList)
        }
    }

    fun retryLoading() {
        _isLoading.value = true
        _hasError.value = false
        loadDrinks()
    }

    private fun loadDrinks() {
        viewModelScope.launch {
            _isLoading.value = true
            _hasError.value = false

            val letters = 'a'..'z'
            val results = letters.map { letter ->
                async {
                    try {
                        val response = RetrofitInstance.api.getDrinksByLetter(letter)
                        response.drinks?.mapNotNull { it.toDrink() } ?: emptyList()
                    } catch (e: Exception) {
                        Log.e("DrinkViewModel", "Error loading drinks for '$letter': ${e.message}", e)
                        emptyList<Drink>()
                    }
                }
            }

            val allDrinks = results.awaitAll().flatten()

            _drinks.value = allDrinks
            _isLoading.value = false
            _hasError.value = allDrinks.isEmpty()  // Only set error if nothing succeeded
        }
    }

    fun refreshRecentlyViewed() {
        collectTo(drinkPrefs.getRecentlyViewed(), _recentlyViewed)
    }

    fun onDrinkViewed(drink: Drink) {
        viewModelScope.launch {
            val current = _recentlyViewed.value.toMutableList()
            current.removeAll { it.id == drink.id }
            current.add(0, drink)
            val newList = current.take(MAX_RECENT_VIEWED)
            _recentlyViewed.value = newList
            drinkPrefs.saveRecentlyViewed(newList)
        }
    }
}
