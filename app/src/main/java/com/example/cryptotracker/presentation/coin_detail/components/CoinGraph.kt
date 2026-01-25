package com.example.cryptotracker.presentation.coin_detail.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cryptotracker.common.formatCompactNumber
import kotlin.math.roundToInt

@Composable
fun CoinGraph(
    prices: List<Double>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary
) {
    if (prices.isEmpty()) return

    // 1. State to track interactivity
    // null means user is not touching the graph.
    // Int holds the index of the price point being touched.
    var selectedIndex by remember(prices) { mutableStateOf<Int?>(null) }

    val minPrice = remember(prices) { prices.min() }
    val maxPrice = remember(prices) { prices.max() }
    val priceRange = maxPrice - minPrice

    // Colors for interactivity elements
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val surfaceColor = MaterialTheme.colorScheme.surface
    val density = LocalDensity.current

    // Paint object for drawing text directly on Canvas
    val textPaint = remember(density) {
        Paint().apply {
            color = onSurfaceColor.toArgb()
            textAlign = Paint.Align.CENTER
            textSize = density.run { 14.sp.toPx() } // Set text size
            isAntiAlias = true
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            // 2. Capture Touch Events
            .pointerInput(prices) {
                detectTapGestures(
                    onPress = { offset ->
                        // When touched, calculate the index based on X position
                        val width = size.width.toFloat()
                        val spacing = width / (prices.size - 1)

                        // Now the calculation matches the Canvas drawing logic exactly
                        val index = (offset.x / spacing).roundToInt()

                        selectedIndex = index.coerceIn(0, prices.lastIndex)
                        tryAwaitRelease()
                        selectedIndex = null// Reset when lifted
                    }
                    // Note: For smoother dragging, we might need detectHorizontalDragGestures
                    // but onPress combined with tryAwaitRelease often handles simple drags well enough for charts.
                )
            }
    ) {
        val width = size.width
        val height = size.height
        val spacing = width / (prices.size - 1)

        // Helper function to calculate coordinates for any index
        // We extract this so both the main graph and the interactive marker can use it.
        fun getCoordinatesForIndex(index: Int): Offset {
            val price = prices[index]
            val x = index * spacing
            val normalizedPrice = (price - minPrice) / priceRange
            val y = height - (normalizedPrice * height).toFloat()
            return Offset(x, y)
        }

        // --- PART A: Draw the Base Graph (Your existing code) ---
        val path = Path().apply {
            prices.indices.forEach { i ->
                val coords = getCoordinatesForIndex(i)
                if (i == 0) moveTo(coords.x, coords.y) else lineTo(coords.x, coords.y)
            }
        }

        // Optional: Gradient Fill
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

        // Main Line
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx())
        )

        // --- PART B: Draw Interactivity (The new part) ---
        selectedIndex?.let { index ->
            // 1. Get exact position for the selected index
            val point = getCoordinatesForIndex(index)
            val selectedPrice = prices[index]

            // 2. Draw Vertical Guideline
            drawLine(
                color = onSurfaceColor.copy(alpha = 0.5f),
                start = Offset(point.x, 0f),
                end = Offset(point.x, height),
                strokeWidth = 2.dp.toPx(),
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
            )

            // 3. Draw Highlight Circle on the line
            drawCircle(
                color = surfaceColor,
                radius = 6.dp.toPx(),
                center = point
            )
            drawCircle(
                color = lineColor,
                radius = 4.dp.toPx(),
                center = point
            )

            // 4. Draw Tooltip Box and Text
            val priceText = "$${formatCompactNumber(selectedPrice)}"
            // Calculate text dimensions roughly
            val textWidth = textPaint.measureText(priceText)
            val textHeight = textPaint.descent() - textPaint.ascent()
            val padding = 8.dp.toPx()

            // Position tooltip above the point, shifting if near edges
            var boxX = point.x - (textWidth / 2) - padding
            var boxY = point.y - textHeight - (padding * 3)

            // Clamp box to screen bounds so it doesn't fall off edges
            boxX = boxX.coerceIn(0f, width - textWidth - padding * 2)
            boxY = boxY.coerceIn(0f, height - textHeight - padding * 2)

            // Draw Tooltip Background Box
            drawRoundRect(
                color = surfaceColor,
                topLeft = Offset(boxX, boxY),
                size = Size(textWidth + padding * 2, textHeight + padding * 2),
                cornerRadius = CornerRadius(8.dp.toPx()),
                style = androidx.compose.ui.graphics.drawscope.Fill
            )
            // Draw Border for box
            drawRoundRect(
                color = lineColor,
                topLeft = Offset(boxX, boxY),
                size = Size(textWidth + padding * 2, textHeight + padding * 2),
                cornerRadius = CornerRadius(8.dp.toPx()),
                style = Stroke(width = 1.dp.toPx())
            )

            // Draw Text using native canvas
            drawContext.canvas.nativeCanvas.drawText(
                priceText,
                boxX + padding + (textWidth / 2), // Center X pos
                boxY + padding + textHeight - textPaint.descent(), // Baseline Y pos
                textPaint
            )
        }
    }
}