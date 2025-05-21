package com.am.drinks.drinks

import java.io.Serializable

data class Drink(
    val id: String,
    val name: String,
    val type: String,
    val category: String,
    val imageUrl: String? = null,
    var personalBest: Int? = null,
    val instructions: String? = null,
    val glass: String? = null,
    val iba: String? = null,
    val ingredients: List<Pair<String, String?>>
) : Serializable // for sending between views