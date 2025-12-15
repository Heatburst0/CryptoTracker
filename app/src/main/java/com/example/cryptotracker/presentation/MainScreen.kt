package com.example.cryptotracker.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.cryptotracker.presentation.coin_detail.CoinDetailScreen
import com.example.cryptotracker.presentation.coin_list.CoinListScreen
import com.example.cryptotracker.presentation.favorites.FavoritesScreen
import com.example.cryptotracker.presentation.navigation.BottomNavItem
import com.example.cryptotracker.presentation.navigation.components.BottomNavigationBar

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    // Define your tabs here
    val bottomNavItems = listOf(
        BottomNavItem(
            name = "Home",
            route = Screen.CoinListScreen.route,
            icon = Icons.Default.Home
        ),
        BottomNavItem(
            name = "Watchlist",
            route = Screen.FavoritesScreen.route,
            icon = Icons.Default.Star
        )
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(
                    items = bottomNavItems,
                    navController = navController,
                    onItemClick = { item ->
                        navController.navigate(item.route) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            popUpTo(Screen.CoinListScreen.route) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = Screen.CoinListScreen.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.CoinListScreen.route) {
                CoinListScreen(navController = navController)
            }
            composable(Screen.FavoritesScreen.route) {
                FavoritesScreen(navController = navController)
            }
            composable(Screen.CoinDetailScreen.route + "/{coinId}") {
                CoinDetailScreen(navController = navController)
            }
        }
    }
}