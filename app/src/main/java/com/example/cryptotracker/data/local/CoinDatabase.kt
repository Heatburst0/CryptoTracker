package com.example.cryptotracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CoinEntity::class],
    version = 1,
    exportSchema = false // Set to true if you want to export schema for migrations
)
abstract class CoinDatabase : RoomDatabase() {
    abstract val dao: CoinDao
}