package com.example.cryptotracker.presentation.coin_list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.cryptotracker.common.Constants.parseErrorMessage
import com.example.cryptotracker.presentation.Screen
import com.example.cryptotracker.presentation.coin_list.components.CoinListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinListScreen(
    navController: NavController,
    viewModel: CoinListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val coins = viewModel.coinsPagingFlow.collectAsLazyPagingItems()

    // Local state to toggle search bar visibility
    var isSearchActive by remember { mutableStateOf(false) }

    // Calculate refreshing state based on Mediator or standard Refresh
    val isRefreshing = coins.loadState.refresh is LoadState.Loading

    val isSearchMode = state.searchQuery.isNotEmpty()
    val haveCoins = coins.itemCount > 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        TextField(
                            value = state.searchQuery,
                            onValueChange = { viewModel.onSearch(it) },
                            placeholder = { Text("Search coin...") },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(text = "Crypto Tracker")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if(isSearchActive) {
                            viewModel.onSearch("") // Clear text on close
                        }
                        isSearchActive = !isSearchActive
                    }) {
                        Icon(
                            imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if(isSearchMode){
                // ------------------------------------------------
                // MODE A: SEARCH (Database Cache + API)
                // ------------------------------------------------
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.coins) { coin ->
                        CoinListItem(
                            coin = coin,
                            onItemClick = {
                                navController.navigate(Screen.CoinDetailScreen.route + "/${coin.id}")
                                isSearchActive = false
                                viewModel.clearSearch()
                            }
                        )
                    }
                }

                if (state.coins.isEmpty() && !state.isLoading && state.error.isBlank()) {
                    Text(
                        text = "No results found",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                if (state.error.isNotBlank()) {
                    Text(
                        text = state.error,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            } else {
                // ------------------------------------------------
                // MODE B: BROWSING (Paging 3 + RemoteMediator)
                // ------------------------------------------------

                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { coins.refresh() }
                ) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {

                        // 1. Coin Items
                        items(
                            count = coins.itemCount,
                            key = { index -> coins[index]?.id ?: index }
                        ) { index ->
                            val coin = coins[index]
                            if (coin != null) {
                                CoinListItem(
                                    coin = coin,
                                    onItemClick = {
                                        navController.navigate(Screen.CoinDetailScreen.route + "/${coin.id}")
                                    }
                                )
                            }
                        }

                        // 2. Footer Loading/Error State (Appending)
                        // FIX: Check BOTH standard state AND Mediator state
                        val appendState = coins.loadState.append

                        val mediatorState = coins.loadState.mediator

                        when {
                            appendState is LoadState.Loading -> {
                                item {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) { CircularProgressIndicator() }
                                }
                            }
                            // If either PagingSource OR RemoteMediator failed, show Retry
                            appendState is LoadState.Error ||
                                    mediatorState?.refresh is LoadState.Error ||
                                    mediatorState?.append is LoadState.Error -> {
                                val error = (appendState as? LoadState.Error)?.error
                                    ?: (mediatorState?.append as? LoadState.Error)?.error
                                    ?: (mediatorState?.refresh as? LoadState.Error)?.error

                                if (error != null) { // Only show if we actually found an error
                                    item {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "Could not load more coins", // Simpler message usually better here
                                                color = MaterialTheme.colorScheme.error
                                            )
                                            Button(onClick = { coins.retry() }) {
                                                Text("Retry")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                val refreshState = coins.loadState.refresh

// FIX: Add the third check below
                if (refreshState is LoadState.Error && !haveCoins && coins.loadState.source.refresh !is LoadState.Loading) {

                    val error = (refreshState as LoadState.Error).error
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = parseErrorMessage(error),
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Button(onClick = { coins.retry() }) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}