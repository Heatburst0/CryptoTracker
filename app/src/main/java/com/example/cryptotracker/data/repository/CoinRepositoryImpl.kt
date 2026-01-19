package com.example.cryptotracker.data.repository

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.cryptotracker.common.Resource
import com.example.cryptotracker.data.local.CoinDatabase
import com.example.cryptotracker.data.local.toCoin
import com.example.cryptotracker.data.remote.CoinGeckoApi
import com.example.cryptotracker.data.local.toEntity
import com.example.cryptotracker.data.remote.CoinRemoteMediator
import com.example.cryptotracker.data.remote.dto.CoinDto
import com.example.cryptotracker.data.remote.dto.toCoin
import com.example.cryptotracker.domain.repository.CoinRepository
import com.example.cryptotracker.domain.model.Coin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

import javax.inject.Inject

class CoinRepositoryImpl @Inject constructor(
    private val api: CoinGeckoApi,
    private val db: CoinDatabase
) : CoinRepository {

    private val dao = db.coinDao
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

    override suspend fun toggleFavoriteCoin(id: String, isFavorite: Boolean) {
        dao.updateFavoriteStatus(id, isFavorite)
        Log.e("Favorite","Favorite added")
    }

    override fun getFavoriteCoins(): Flow<Resource<List<Coin>>> = flow {
        emit(Resource.Loading(true))

        dao.getFavoriteCoins().collect { entities ->
            emit(Resource.Success(entities.map { it.toCoin() }))
        }

    }

    @OptIn(ExperimentalPagingApi::class)
    override fun getCoinsPaged(): Flow<PagingData<Coin>> {
        return Pager(
            config = PagingConfig(pageSize = 20),

            // Now we can pass 'db' because we have it in the constructor
            remoteMediator = CoinRemoteMediator(api, db),

            pagingSourceFactory = { dao.getCoinsPagingSource() }
        ).flow.map { pagingData ->
            pagingData.map { it.toCoin() }
        }
    }

    override fun searchCoins(query: String): Flow<Resource<List<Coin>>> = flow{
        emit(Resource.Loading(true))

        try{

            val searchResult = api.searchCoins(query)

            if(searchResult.coins.isEmpty()){
                emit(Resource.Success(emptyList()))
                emit(Resource.Loading(false))
                return@flow
            }

            // Extract Top 5 IDs to save bandwidth
            // joinToString creates: "bitcoin,bitcoin-gold,wrapped-bitcoin"
            val ids = searchResult.coins.take(5).joinToString(","){
                it.id
            }
            val marketData = api.getCoins(
                ids = ids,
                perPage = 5,
                page=1,
                sparkline = true
            )
            saveCoinsToDb(marketData)

            val coins = marketData.map { it.toCoin() }
            emit(Resource.Success(coins))
        }catch (e: Exception){
            emit(Resource.Error("Search failed: ${e.localizedMessage}"))
        }
        emit(Resource.Loading(false))
    }

    override suspend fun refreshCoin(coinId: String): Resource<Unit> {
        return try{
            val response = api.getCoins(ids=coinId, perPage = 1, page = 1, sparkline = true)
            saveCoinsToDb(response)
            Resource.Success(Unit)
        }catch (e: Exception){
            Resource.Error("Failed to refresh: ${e.localizedMessage}")
        }
    }

    override suspend fun refreshFavorites(): Resource<Unit> {
        return try{
            val favoriteIds = dao.getFavoriteCoinIds()
            if(favoriteIds.isEmpty()) return Resource.Success(Unit)
            val idsString = favoriteIds.joinToString(",")

            val response = api.getCoins(ids=idsString, perPage = 100, page = 1, sparkline = true)
            saveCoinsToDb(response)

            Resource.Success(Unit)
        }catch (e: Exception){
            Resource.Error("Failed to refresh favorites: ${e.localizedMessage}")
        }
    }

    private suspend fun saveCoinsToDb(remoteCoins: List<CoinDto>) {
        val favoriteIds = dao.getFavoriteCoinIds()

        val coinEntities = remoteCoins.map {
            it.toEntity().copy(
                isFavorite = favoriteIds.contains(it.id)
            )
        }

        dao.insertCoins(coinEntities)
    }
}