package com.example.cryptotracker.domain.model

data class Coin(
    val id: String,
    val name: String,
    val symbol: String,
    val currentPrice: Double,
    val marketCap: Double,
    val image: String,
    val priceChangePercentage24h: Double,
    val high24h: Double,
    val low24h: Double,
    val sparkline: List<Double> = emptyList(),
    val isFavorite: Boolean = false
)