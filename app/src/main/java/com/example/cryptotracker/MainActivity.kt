package com.example.cryptotracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.cryptotracker.presentation.Screen
import com.example.cryptotracker.presentation.coin_detail.CoinDetailScreen
import com.example.cryptotracker.presentation.coin_list.CoinListScreen
import com.example.cryptotracker.presentation.favorites.FavoritesScreen
import com.example.cryptotracker.ui.theme.CryptoTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContent {
            // Note: We haven't created a Theme.kt yet, so use MaterialTheme or
            // the default theme Android Studio created for you (e.g., CryptoTrackerTheme)
            CryptoTrackerTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Scaffold(
                    bottomBar = {
                        if (currentRoute == Screen.CoinListScreen.route || currentRoute == Screen.FavoritesScreen.route){
                            NavigationBar {
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                    label = { Text("Home") },
                                    selected = currentRoute == Screen.CoinListScreen.route,
                                    onClick = {
                                        navController.navigate(Screen.CoinListScreen.route) {
                                            popUpTo(Screen.CoinListScreen.route) { inclusive = true }
                                        }
                                    }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Star, contentDescription = "Watchlist") },
                                    label = { Text("Watchlist") },
                                    selected = currentRoute == Screen.FavoritesScreen.route,
                                    onClick = {
                                        navController.navigate(Screen.FavoritesScreen.route) {
                                            popUpTo(Screen.CoinListScreen.route)
                                        }
                                    }
                                )
                            }
                        }
                    }
                ){ innerPadding ->
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
        }
    }
}
