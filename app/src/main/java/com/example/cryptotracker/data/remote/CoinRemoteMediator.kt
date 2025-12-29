package com.example.cryptotracker.data.remote

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.cryptotracker.data.local.CoinDatabase
import com.example.cryptotracker.data.local.CoinEntity
import com.example.cryptotracker.data.local.CoinRemoteKeys
import com.example.cryptotracker.data.local.toEntity
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class CoinRemoteMediator(
    private val api: CoinGeckoApi,
    private val db: CoinDatabase
) : RemoteMediator<Int, CoinEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, CoinEntity>
    ): MediatorResult {
        return try {
            // 1. Calculate the Page Number based on LoadType
            val page = when (loadType) {
                LoadType.REFRESH -> 1
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val remoteKeys = getRemoteKeyForLastItem(state)
                    val nextKey = remoteKeys?.nextKey
                        ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                    nextKey
                }
            }

            // 2. Fetch API Data
            val response = api.getCoins(
                page = page,
                perPage = state.config.pageSize,
                sparkline = true
            )

            // 3. Save to DB (Transaction ensures data integrity)
            db.withTransaction {
                // If Refreshing, clear old cache (keeping favorites safe)
                if (loadType == LoadType.REFRESH) {
                    db.remoteKeysDao.clearKeys()
                    db.coinDao.clearNonFavorites()
                }

                // Calculate Keys
                val prevKey = if (page == 1) null else page - 1
                val nextKey = if (response.isEmpty()) null else page + 1

                val keys = response.map { coin ->
                    CoinRemoteKeys(id = coin.id, prevKey = prevKey, nextKey = nextKey)
                }

                // Prepare Entities (Preserving isFavorite flag if exists)
                // Note: Since we only cleared non-favorites, the favorites are still in DB.
                // However, onConflict=REPLACE might overwrite them. We need to check IDs.
                val favoriteIds = db.coinDao.getFavoriteCoinIds() // You might need to make this synchronous in DAO

                val entities = response.map { dto ->
                    dto.toEntity().copy(
                        isFavorite = favoriteIds.contains(dto.id)
                    )
                }

                db.remoteKeysDao.insertAll(keys)
                db.coinDao.insertCoins(entities)
            }

            MediatorResult.Success(endOfPaginationReached = response.isEmpty())

        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        }
    }

    // Helper to find the last item loaded to calculate Next Key
    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, CoinEntity>): CoinRemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { coin -> db.remoteKeysDao.getRemoteKeys(coin.id) }
    }
}