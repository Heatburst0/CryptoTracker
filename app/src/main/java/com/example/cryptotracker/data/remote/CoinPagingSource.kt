package com.example.cryptotracker.data.remote

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.cryptotracker.data.local.CoinDao
import com.example.cryptotracker.data.local.toCoin
import com.example.cryptotracker.data.remote.dto.CoinDto
import com.example.cryptotracker.data.remote.dto.toCoin
import com.example.cryptotracker.domain.model.Coin
import kotlin.time.Duration.Companion.minutes

class CoinPagingSource(
    private val api : CoinGeckoApi,
    private val dao: CoinDao,
    private val onCoinsFetched: suspend (List<CoinDto>) -> Unit
) : PagingSource<Int, Coin>() {
    override fun getRefreshKey(state: PagingState<Int, Coin>): Int? {


        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Coin> {
        val page = params.key ?: 1
        return try{


            val response = api.getCoins(
                page = page,
                perPage = params.loadSize,
                sparkline = true
            )
            // 2. CRITICAL FIX: Save to DB immediately!
            onCoinsFetched(response)

            val coins = response.map { it.toCoin() }

            LoadResult.Page(
                data = coins,
                // If page is 1, previous is null.
                prevKey = if (page == 1) null else page - 1,
                // If the response is empty, we are at the end (next is null).
                // Otherwise, next page is current + 1
                nextKey = if (coins.isEmpty()) null else page + 1
            )
        }catch (e: Exception){
            if(page == 1){
                val cachedCoins = dao.getCachedCoins()
                if(cachedCoins.isNotEmpty()){
                    return LoadResult.Page(
                        data = cachedCoins.map { it.toCoin() },
                        prevKey = null,
                        nextKey = 2
                    )
                }
            }
            // If DB is empty or it's page 2+, return the error
            LoadResult.Error(e)
        }
    }


}