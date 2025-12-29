package com.example.cryptotracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [CoinEntity::class, CoinRemoteKeys::class],
    version = 3,
    exportSchema = false // Set to true if you want to export schema for migrations
)
@TypeConverters(Converters::class)
abstract class CoinDatabase : RoomDatabase() {
    abstract val coinDao: CoinDao
    abstract val remoteKeysDao: CoinRemoteKeysDao

    companion object{
        val MIGRATION_2_3 = object : Migration(2,3){
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                CREATE TABLE IF NOT EXISTS `remote_keys` (
                    `id` TEXT NOT NULL, 
                    `prevKey` INTEGER, 
                    `nextKey` INTEGER, 
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
                )
            }
        }
    }
}