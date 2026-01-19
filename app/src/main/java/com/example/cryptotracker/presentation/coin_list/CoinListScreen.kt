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
import com.example.cryptotracker.presentation.coin_list.components.CoinListTopBar
import com.example.cryptotracker.presentation.coin_list.components.PagedCoinList
import com.example.cryptotracker.presentation.coin_list.components.SearchCoinList

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

    val isSearchMode = state.searchQuery.isNotEmpty()

    Scaffold(
        topBar = {
            CoinListTopBar(
                isSearchActive = isSearchActive,
                searchQuery = state.searchQuery,
                onSearchQueryChange = { viewModel.onSearch(it)},
                onToggleSearch = {
                    if (isSearchActive) viewModel.onSearch("")
                    isSearchActive = !isSearchActive
                }
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier.fillMaxSize()
                .padding(paddingValues)
        ){
            if (isSearchMode) {
                SearchCoinList(
                    state = state,
                    onItemClick = { coinId ->
                        navController.navigate(Screen.CoinDetailScreen.route + "/$coinId")
                        isSearchActive = false
                        viewModel.clearSearch()
                    }
                )
            } else {
                PagedCoinList(
                    coins = coins,
                    onItemClick = { coinId ->
                        navController.navigate(Screen.CoinDetailScreen.route + "/$coinId")
                    }
                )
            }
        }

    }
}