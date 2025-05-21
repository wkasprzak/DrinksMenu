package com.am.drinks.drinks

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface CocktailApiService {
    @GET("search.php")
    suspend fun getDrinksByLetter(@Query("f") letter: Char): DrinkResponse
}

object RetrofitInstance {
    val api: CocktailApiService = Retrofit.Builder()
        .baseUrl("https://www.thecocktaildb.com/api/json/v1/1/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(CocktailApiService::class.java)
}