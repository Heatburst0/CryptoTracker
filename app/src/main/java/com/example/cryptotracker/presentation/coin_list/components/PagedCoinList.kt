package com.example.cryptotracker.presentation.coin_list.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.example.cryptotracker.common.Constants.parseErrorMessage
import com.example.cryptotracker.common.ErrorFooter
import com.example.cryptotracker.common.FullScreenError
import com.example.cryptotracker.domain.model.Coin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PagedCoinList(
    coins: LazyPagingItems<Coin>,
    onItemClick: (String) ->Unit
    ) {
    val loadState = coins.loadState
    val isRefreshing = loadState.refresh is LoadState.Loading
    val haveCoins = coins.itemCount > 0
    val sourceLoading = loadState.source.refresh is LoadState.Loading

    val shouldShowFullScreenError = loadState.refresh is LoadState.Error && !haveCoins && !sourceLoading
    val shouldShowFullScreenLoading = loadState.refresh is LoadState.Loading && !haveCoins

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { coins.refresh() }
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {

            items(
                count = coins.itemCount,
                key= { index -> coins[index]?.id ?: index }
            ){ index ->

                coins[index]?.let{ coin->

                    CoinListItem(
                        coin = coin,
                    ) {
                        onItemClick(coin.id)
                    }
                }

            }
            //Footer logic

            val appendState = loadState.append
            val mediatorState = loadState.mediator

            when{
                appendState is LoadState.Loading ->{
                    item{
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator() }
                    }
                }

                appendState is LoadState.Error ||
                        mediatorState?.refresh is LoadState.Error ||
                        mediatorState?.append is LoadState.Error -> {
                    val error = (appendState as? LoadState.Error)?.error
                        ?: (mediatorState?.append as? LoadState.Error)?.error
                        ?: (mediatorState?.refresh as? LoadState.Error)?.error

                    if (error != null && haveCoins) {
                        item {
                            ErrorFooter(
                                message = "Could not load more coins",
                                onRetry = { coins.retry() }
                            )
                        }
                    }
                }
            }
        }
        if(shouldShowFullScreenError){
            val error = (loadState.refresh as LoadState.Error).error

            FullScreenError(
                message = parseErrorMessage(error),
            ) {
                coins.retry()
            }
        }
        if (shouldShowFullScreenLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}