package com.example.cryptotracker.di

import android.app.Application
import androidx.room.Room
import com.example.cryptotracker.common.Constants
import com.example.cryptotracker.data.local.CoinDao
import com.example.cryptotracker.data.local.CoinDatabase
import com.example.cryptotracker.data.remote.CoinGeckoApi
import com.example.cryptotracker.data.repository.CoinRepositoryImpl
import com.example.cryptotracker.domain.repository.CoinRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Installs these dependencies in the Application scope
object AppModule {

    // 1. Provide Retrofit API
    @Provides
    @Singleton
    fun providePaprikaApi(): CoinGeckoApi {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(CoinGeckoApi::class.java)
    }

    // 2. Provide Room Database
    @Provides
    @Singleton
    fun provideCoinDatabase(app: Application): CoinDatabase {
        return Room.databaseBuilder(
            app,
            CoinDatabase::class.java,
            "coin_db"
        ).build()
    }

    // 3. Provide DAO (We need the DB to get the DAO)
    @Provides
    @Singleton
    fun provideCoinDao(db: CoinDatabase): CoinDao {
        return db.dao
    }

    // 4. Provide Repository Implementation
    // Note: We return the Interface (CoinRepository), but inject the Implementation (CoinRepositoryImpl)
    // This allows us to easily swap the Impl with a FakeRepository during testing.
    @Provides
    @Singleton
    fun provideCoinRepository(api: CoinGeckoApi, dao: CoinDao): CoinRepository {
        return CoinRepositoryImpl(api, dao)
    }
}