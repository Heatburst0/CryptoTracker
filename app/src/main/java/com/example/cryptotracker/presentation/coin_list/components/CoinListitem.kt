package com.example.cryptotracker.presentation.coin_list.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.cryptotracker.domain.model.Coin

@Composable
fun CoinListItem(
    coin: Coin,
    onItemClick: (Coin) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onItemClick(coin) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
            // REMOVED: horizontalArrangement = Arrangement.SpaceBetween
            // Reason: We want manual control over spacing using weight
        ) {
            // 1. IMAGE (Fixed Size)
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(coin.image)
                    .crossfade(true)
                    .diskCachePolicy(CachePolicy.ENABLED) // Ensure Disk Cache is ON
                    .networkCachePolicy(CachePolicy.ENABLED)
                    .build(),
                contentDescription = coin.name,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(20.dp))
            )

            Spacer(modifier = Modifier.width(16.dp))

            // 2. NAME & SYMBOL (Flexible Width)
            // Modifier.weight(1f) tells this column: "Take all remaining space, but respect your neighbors."
            Column(
                modifier = Modifier.weight(1f) // <--- CRITICAL FIX
            ) {
                Text(
                    text = coin.symbol.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = coin.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1, // <--- Add this
                    overflow = TextOverflow.Ellipsis // <--- Add this (adds "..." if too long)
                )
            }

            // 3. PRICE & CHANGE (Intrinsic Width)
            // Since we didn't give this a weight, it takes exactly the size it needs.
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "$${coin.currentPrice}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                val isProfit = coin.priceChangePercentage24h > 0
                val changeColor = if (isProfit) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isProfit) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = changeColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "${coin.priceChangePercentage24h}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = changeColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}