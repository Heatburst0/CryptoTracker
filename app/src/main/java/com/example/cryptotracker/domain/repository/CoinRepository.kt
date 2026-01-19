package com.example.cryptotracker.domain.repository

import androidx.paging.PagingData
import com.example.cryptotracker.common.Resource
import com.example.cryptotracker.domain.model.Coin
import kotlinx.coroutines.flow.Flow

interface CoinRepository {

    // We return a Flow because the data might be emitted multiple times
    // (once from Cache, then again from Network)
//    fun getCoins(fetchFromRemote: Boolean): Flow<Resource<List<Coin>>>

    // Simple suspend function for single detail lookup
    suspend fun getCoinById(id: String): Resource<Coin>

    suspend fun toggleFavoriteCoin(id: String, isFavorite: Boolean)

    fun getFavoriteCoins(): Flow<List<Coin>>

    fun getCoinsPaged() : Flow<PagingData<Coin>>

    fun searchCoins(query: String) : Flow<Resource<List<Coin>>>

    suspend fun refreshCoin(coinId: String): Resource<Unit>

    suspend fun refreshFavorites() : Resource<Unit>

}