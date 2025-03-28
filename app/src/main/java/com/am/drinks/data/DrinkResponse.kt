package com.am.drinks.data

data class DrinkResponse(
    val drinks: List<ApiDrink>
)

data class CategoryResponse(val drinks: List<CategoryItem>)
data class CategoryItem(val strCategory: String)

data class AlcoholResponse(val drinks: List<AlcoholItem>)
data class AlcoholItem(val strAlcoholic: String)