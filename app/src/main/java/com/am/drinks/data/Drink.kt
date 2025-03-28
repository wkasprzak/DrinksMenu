package com.am.drinks.data
import java.io.Serializable

data class Drink(
    val id: String,
    val name: String,
    val type: String,
    val category: String,
    val imageUrl: String? = null,
    var time: Int = 20,
    val instructions: String? = null,
    val glass: String? = null,
    val iba: String? = null,
    val ingredients: List<String> = emptyList()
) : Serializable