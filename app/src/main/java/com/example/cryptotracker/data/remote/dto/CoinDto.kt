package com.example.cryptotracker.data.remote.dto

import com.example.cryptotracker.domain.model.Coin
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true) // Tells Moshi to generate a fast adapter at compile time
data class CoinDto(
    val id: String,
    val symbol: String,
    val name: String,
    val image: String,
    @Json(name = "current_price") val currentPrice: Double,
    @Json(name = "market_cap") val marketCap: Double,
    @Json(name = "market_cap_rank") val marketCapRank: Int,
    @Json(name = "fully_diluted_valuation") val fullyDilutedValuation: Double?,
    @Json(name = "total_volume") val totalVolume: Double?,
    @Json(name = "high_24h") val high24h: Double,
    @Json(name = "low_24h") val low24h: Double,
    @Json(name = "price_change_24h") val priceChange24h: Double,
    @Json(name = "price_change_percentage_24h") val priceChangePercentage24h: Double
)

// Extension function to map DTO to Domain Model
// This keeps the conversion logic centralized here.
fun CoinDto.toCoin(): Coin {
    return Coin(
        id = id,
        name = name,
        symbol = symbol,
        currentPrice = currentPrice,
        marketCap = marketCap,
        image = image,
        priceChangePercentage24h = priceChangePercentage24h,
        high24h = high24h,
        low24h = low24h
    )
}