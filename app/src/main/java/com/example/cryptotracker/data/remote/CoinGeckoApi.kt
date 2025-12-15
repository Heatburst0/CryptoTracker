package com.example.cryptotracker.data.remote

import com.example.cryptotracker.data.remote.dto.CoinDto
import com.example.cryptotracker.data.remote.dto.SearchResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface CoinGeckoApi {

    @GET("coins/markets")
    suspend fun getCoins(
        @Query("vs_currency") currency: String = "usd",
        @Query("ids") ids: String? = null,
        @Query("order") order: String = "market_cap_desc",
        @Query("per_page") perPage: Int = 50,
        @Query("page") page: Int = 1,
        @Query("sparkline") sparkline: Boolean = false
    ): List<CoinDto>

    // Note: We are returning the DTO here, not the Domain model.
    // The Repository is responsible for converting DTO -> Domain.

    @GET("search")
    suspend fun searchCoins(
        @Query("query") query: String
    ): SearchResponseDto
}