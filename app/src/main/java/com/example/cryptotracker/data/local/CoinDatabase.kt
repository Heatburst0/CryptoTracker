package com.example.cryptotracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [CoinEntity::class],
    version = 2,
    exportSchema = false // Set to true if you want to export schema for migrations
)
@TypeConverters(Converters::class)
abstract class CoinDatabase : RoomDatabase() {
    abstract val dao: CoinDao
}