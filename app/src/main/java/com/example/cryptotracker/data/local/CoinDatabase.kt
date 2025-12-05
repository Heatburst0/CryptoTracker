package com.example.cryptotracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [CoinEntity::class],
    version = 3,
    exportSchema = false // Set to true if you want to export schema for migrations
)
@TypeConverters(Converters::class)
abstract class CoinDatabase : RoomDatabase() {
    abstract val dao: CoinDao

    companion object{
        val MIGRATION_2_3 = object : Migration(2,3){
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE coin_table ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}