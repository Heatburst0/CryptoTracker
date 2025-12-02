package com.example.cryptotracker.presentation.coin_detail.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun CoinGraph(
    prices: List<Double>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary
) {
    if (prices.isEmpty()) return

    // Pre-calculate Min/Max to normalize the graph
    val minPrice = remember(prices) { prices.min() }
    val maxPrice = remember(prices) { prices.max() }
    val priceRange = maxPrice - minPrice

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val spacing = width / (prices.size - 1)

        val path = Path().apply {
            prices.forEachIndexed { i, price ->
                val x = i * spacing
                // Normalize Y: (price - min) / range gives 0..1.
                // We invert it (1 - result) because Canvas Y=0 is top.
                val normalizedPrice = (price - minPrice) / priceRange
                val y = height - (normalizedPrice * height).toFloat()

                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx())
        )

        // Optional: Add a gradient fill below the line
        val fillPath = android.graphics.Path(path.asAndroidPath()).asComposePath().apply {
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.3f), Color.Transparent),
                endY = height
            )
        )
    }
}