package com.example.cryptotracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CoinDao {

    // UPSERT: Update if exists, Insert if new.
    // This is crucial for refreshing data without duplicates.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoins(coins: List<CoinEntity>)

    // Delete everything (optional, if we want to clear cache)
    @Query("DELETE FROM coin_table")
    suspend fun clearCoins()

    // Get all coins (observable Flow)
    @Query("SELECT * FROM coin_table")
    fun getCoins(): kotlinx.coroutines.flow.Flow<List<CoinEntity>>

    // Get single coin
    @Query("SELECT * FROM coin_table WHERE id = :id")
    suspend fun getCoinById(id: String): CoinEntity?
}