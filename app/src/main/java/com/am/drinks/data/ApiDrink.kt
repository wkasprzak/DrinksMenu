package com.am.drinks.data

data class ApiDrink(
    val idDrink: String?,
    val strDrink: String?,
    val strCategory: String?,
    val strAlcoholic: String?,
    val strInstructions: String?,
    val strDrinkThumb: String?,
    val strGlass: String?,
    val strIBA: String?,
    val strIngredient1: String?,
    val strIngredient2: String?,
    val strIngredient3: String?,
    val strIngredient4: String?,
    val strIngredient5: String?,
    val strIngredient6: String?,
    val strIngredient7: String?,
    val strIngredient8: String?,
    val strIngredient9: String?,
    val strIngredient10: String?
)

fun ApiDrink.toDrink(): Drink? {
    val id = idDrink ?: return null
    val name = strDrink ?: return null
    val ingredients = listOf(
        strIngredient1, strIngredient2, strIngredient3, strIngredient4, strIngredient5,
        strIngredient6, strIngredient7, strIngredient8, strIngredient9, strIngredient10
    ).filterNotNull().filter { it.isNotBlank() }

    return Drink(
        id = id,
        name = name,
        type = strAlcoholic ?: "Unknown",
        category = strCategory ?: "Unknown",
        imageUrl = strDrinkThumb,
        glass = strGlass,
        iba = strIBA,
        instructions = strInstructions,
        ingredients = ingredients
    )
}
