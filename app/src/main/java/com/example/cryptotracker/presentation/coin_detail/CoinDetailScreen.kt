package com.example.cryptotracker.presentation.coin_detail

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
// Imports from our new locations
import com.example.cryptotracker.common.formatCompactNumber
import com.example.cryptotracker.presentation.coin_detail.components.CoinGraph
import com.example.cryptotracker.presentation.coin_detail.components.StatCard
import com.example.cryptotracker.presentation.coin_detail.components.TimeSpanSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinDetailScreen(
    navController: NavController,
    viewModel: CoinDetailViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState().value
    var selectedTimeSpan by remember { mutableStateOf("7 Days") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.coin?.name ?: "Coin Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {

            state.coin?.let { coin ->
                // Data Logic
                val filteredPrices = remember(selectedTimeSpan, coin.sparkline) {
                    val fullList = coin.sparkline
                    if (fullList.isEmpty()) return@remember emptyList()
                    when(selectedTimeSpan) {
                        "1 Day" -> fullList.takeLast(24)
                        "3 Days" -> fullList.takeLast(72)
                        else -> fullList
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // 1. Header (Image + Symbol)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = coin.image,
                            contentDescription = coin.name,
                            modifier = Modifier.size(64.dp).clip(RoundedCornerShape(32.dp))
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = coin.symbol.uppercase(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 2. Price
                    Text(
                        text = "$${coin.currentPrice}",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold
                    )

                    val changeColor = if (coin.priceChangePercentage24h > 0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                    Text(
                        text = "${coin.priceChangePercentage24h}% (24h)",
                        color = changeColor,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // 3. Graph Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Price History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        TimeSpanSelector(selected = selectedTimeSpan, onSelectionChange = { selectedTimeSpan = it })
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                            .padding(16.dp)
                    ) {
                        Column {
                            Text("Price ($)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.height(8.dp))
                            CoinGraph(
                                prices = filteredPrices,
                                lineColor = changeColor,
                                modifier = Modifier.fillMaxWidth().height(200.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Time ($selectedTimeSpan)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.align(Alignment.End))
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // 4. Statistics Grid (Using Helper Function)
                    Text("Market Stats", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        StatCard("Market Cap", "$${formatCompactNumber(coin.marketCap)}", Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(16.dp))
                        StatCard("High 24h", "$${coin.high24h}", Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        StatCard("Low 24h", "$${coin.low24h}", Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(16.dp))
                        StatCard("Rank", "#${coin.marketCap}", Modifier.weight(1f))
                    }
                }
            }

            // Loading/Error Views
            if(state.error.isNotBlank()) {
                Text(text = state.error, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
            }
            if(state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}