package com.example.cryptotracker.presentation.coin_list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.cryptotracker.presentation.Screen
import com.example.cryptotracker.presentation.coin_list.components.CoinListItem

@OptIn(ExperimentalMaterial3Api::class) // TopAppBar is experimental in M3
@Composable
fun CoinListScreen(
    navController: NavController,
    viewModel: CoinListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // Local state to toggle search bar visibility
    var isSearchActive by remember { mutableStateOf(false) }

    // Filter Logic: Filter the list based on the query
    val filteredCoins = remember(state.coins, state.searchQuery) {
        if (state.searchQuery.isBlank()) {
            state.coins
        } else {
            state.coins.filter {
                it.name.contains(state.searchQuery, ignoreCase = true) ||
                        it.symbol.contains(state.searchQuery, ignoreCase = true)
            }
        }
    }

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
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredCoins) { coin ->
                    CoinListItem(
                        coin = coin,
                        onItemClick = {
                            navController.navigate(Screen.CoinDetailScreen.route + "/${coin.id}")
                        }
                    )
                }
            }

            // Show Error
            if (state.error.isNotBlank() && filteredCoins.isEmpty()) {
                Text(
                    text = state.error,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .align(Alignment.Center)
                )
            }

            // Show Loading (Only if list is empty)
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            // Show "No Results" if search finds nothing
            if (filteredCoins.isEmpty() && !state.isLoading && state.error.isBlank()) {
                Text(
                    text = "No coins found",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}