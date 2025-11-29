package com.example.cryptotracker.presentation.coin_detail

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

@Composable
fun CoinDetailScreen(
    viewModel: CoinDetailViewModel = hiltViewModel()
){
    val state = viewModel.state.collectAsState().value
    Box(modifier = Modifier.fillMaxSize()) {
        state.coin?.let { coin ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Image
                AsyncImage(
                    model = coin.image,
                    contentDescription = coin.name,
                    modifier = Modifier
                        .size(80.dp)
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(15.dp))

                // Name
                Text(
                    text = "${coin.name} (${coin.symbol.uppercase()})",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(15.dp))

                // Price
                Text(
                    text = "Current Price: $${coin.currentPrice}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(15.dp))

                // High/Low
                Text(
                    text = "High 24h: $${coin.high24h}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Low 24h: $${coin.low24h}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if(state.error.isNotBlank()) {
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

        if(state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }

}