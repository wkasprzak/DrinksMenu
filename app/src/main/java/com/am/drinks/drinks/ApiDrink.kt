package com.am.drinks.drinks

// Drink from API (with its fields)
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
    val strIngredient10: String?,
    val strIngredient11: String?,
    val strIngredient12: String?,
    val strIngredient13: String?,
    val strIngredient14: String?,
    val strIngredient15: String?,
    val strMeasure1: String?,
    val strMeasure2: String?,
    val strMeasure3: String?,
    val strMeasure4: String?,
    val strMeasure5: String?,
    val strMeasure6: String?,
    val strMeasure7: String?,
    val strMeasure8: String?,
    val strMeasure9: String?,
    val strMeasure10: String?,
    val strMeasure11: String?,
    val strMeasure12: String?,
    val strMeasure13: String?,
    val strMeasure14: String?,
    val strMeasure15: String?
)

fun ApiDrink.toDrink(): Drink? {
    val id = idDrink ?: return null
    val name = strDrink ?: return null

    val ingredientList = listOf(
        strIngredient1, strIngredient2, strIngredient3, strIngredient4, strIngredient5,
        strIngredient6, strIngredient7, strIngredient8, strIngredient9, strIngredient10,
        strIngredient11, strIngredient12, strIngredient13, strIngredient14, strIngredient15
    )

    val measureList = listOf(
        strMeasure1, strMeasure2, strMeasure3, strMeasure4, strMeasure5,
        strMeasure6, strMeasure7, strMeasure8, strMeasure9, strMeasure10,
        strMeasure11, strMeasure12, strMeasure13, strMeasure14, strMeasure15
    )

    // combining ingredient with measurement if exists
    val ingredients = ingredientList.zip(measureList).mapNotNull { (ingredient, measure) ->
        if (!ingredient.isNullOrBlank()) {
            Pair(ingredient.trim(), measure?.trim())
        } else null
    }

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
