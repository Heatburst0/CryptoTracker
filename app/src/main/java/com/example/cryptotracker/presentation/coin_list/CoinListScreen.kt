package com.example.cryptotracker.presentation.coin_list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
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

@OptIn(ExperimentalMaterial3Api::class) // TopAppBar is experimental in M3
@Composable
fun CoinListScreen(
    navController: NavController,
    viewModel: CoinListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val coins = viewModel.coinsPagingFlow.collectAsLazyPagingItems()

    // Local state to toggle search bar visibility
    var isSearchActive by remember { mutableStateOf(false) }

    val isSearchMode = state.searchQuery.isNotEmpty()

    // Filter Logic: Filter the list based on the query

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        // Search Field
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
                        // Regular Title
                        Text(text = "Crypto Tracker")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if(isSearchActive) {
                            // If closing search, clear text
                            viewModel.onSearch("")
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
                .padding(paddingValues) // Respect TopBar height
        ) {
            if(isSearchMode){

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

                // Loading State for Search
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                // Error State for Search
                if (state.error.isNotBlank()) {
                    Text(
                        text = state.error,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }else{
                // ------------------------------------------------
                // MODE B: BROWSING (Paging 3 Infinite Scroll)
                // ------------------------------------------------

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    // 1. The Coin Items
                    items(coins.itemCount,
                        key = { index ->
                            coins[index]?.id ?: index
                        }
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

                    // 2. Footer Loading State (Appending)
                    when (val appendState = coins.loadState.append) {
                        is LoadState.Loading -> {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                        is LoadState.Error -> {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = parseErrorMessage(appendState.error),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { coins.retry() },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Text("Retry")
                                    }
                                }
                            }
                        }
                        else -> {}
                    }
                }

                // 3. Full Screen Loading/Error (Refresh)
                when (val refreshState = coins.loadState.refresh) {
                    is LoadState.Loading -> {
                        // Only show spinner if we have NO items (otherwise keep showing cached list)
                        if (coins.itemCount == 0) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                    }
                    is LoadState.Error -> {
                        // Only show full screen error if DB is empty AND Net failed
                        if (coins.itemCount == 0) {
                            Column(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(16.dp),
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
                                    text = parseErrorMessage(refreshState.error),
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { coins.retry() }) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                    else -> {}
                }

            }
        }
    }
}