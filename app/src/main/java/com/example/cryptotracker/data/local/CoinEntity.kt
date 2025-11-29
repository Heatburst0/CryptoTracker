package com.example.cryptotracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.cryptotracker.domain.model.Coin

@Entity(tableName = "coin_table")
data class CoinEntity(
    @PrimaryKey val id: String, // "bitcoin", "ethereum"
    val name: String,
    val symbol: String,
    val currentPrice: Double,
    val marketCap: Double,
    val image: String,
    val priceChangePercentage24h: Double,
    val high24h: Double,
    val low24h: Double
)

// Mapper: Entity -> Domain
// We need this to convert Database objects back to UI objects
fun CoinEntity.toCoin(): Coin {
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

// Mapper: DTO -> Entity
// We need this to save Network data into the Database
fun com.example.cryptotracker.data.remote.dto.CoinDto.toEntity(): CoinEntity {
    return CoinEntity(
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