package com.example.cryptotracker.data.repository

import com.example.cryptotracker.common.Resource
import com.example.cryptotracker.data.local.CoinDao
import com.example.cryptotracker.data.local.toCoin
import com.example.cryptotracker.data.remote.CoinGeckoApi
import com.example.cryptotracker.data.local.toEntity
import com.example.cryptotracker.domain.repository.CoinRepository
import com.example.cryptotracker.domain.model.Coin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class CoinRepositoryImpl @Inject constructor(
    private val api: CoinGeckoApi,
    private val dao: CoinDao
) : CoinRepository {

    override fun getCoins(fetchFromRemote: Boolean): Flow<Resource<List<Coin>>> = flow {
        // 1. Emit Loading State immediately
        emit(Resource.Loading(true))

        // 2. Query Local Database (Source of Truth)
        val localCoins = dao.getCoins().first() // We take a snapshot of the DB

        // 3. Emit Local Data (even if empty, or old)
        // We map Entity -> Domain Model here
        emit(Resource.Success(
            data = localCoins.map { it.toCoin() }
        ))

        // 4. If we want fresh data...
        val isDbEmpty = localCoins.isEmpty()
        val shouldLoadFromCache = !isDbEmpty && !fetchFromRemote

        if (shouldLoadFromCache) {
            emit(Resource.Loading(false))
            return@flow
        }

        // 5. Fetch from Network
        try {
            val remoteCoins = api.getCoins(
                sparkline = true
            ) // Network Call

            // 6. Save to Database (Single Source of Truth)
            // We clear old cache and insert new.
            // Ideally, we should diff, but for now clear/insert is safer.
            dao.clearCoins()
            dao.insertCoins(remoteCoins.map { it.toEntity() })

            // 7. We don't emit data here!
            // We re-query the database to ensure we only show what was actually saved.
            // This guarantees the "Single Source of Truth" principle.
            val newLocalCoins = dao.getCoins().first()
            emit(Resource.Success(newLocalCoins.map { it.toCoin() }))

        } catch (e: IOException) {
            // Internet issue
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        } catch (e: HttpException) {
            // Server issue (404, 500)
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occurred"))
        }

        emit(Resource.Loading(false))
    }

    override suspend fun getCoinById(id: String): Resource<Coin> {
        try {
            // 1. Check Local Database first (Fast & Offline-ready)
            val localCoin = dao.getCoinById(id)

            return if (localCoin != null) {
                Resource.Success(localCoin.toCoin())
            } else {
                // Edge Case: User somehow clicked a coin that isn't in the DB
                // (Rare, but possible if DB was cleared)
                Resource.Error("Coin not found in database")
            }
        } catch (e: Exception) {
            return Resource.Error("Error fetching coin details")
        }
    }
}