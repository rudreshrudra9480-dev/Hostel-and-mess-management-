package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.absoluteValue

@Composable
fun DynamicQRCodeView(
    data: String,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    darkColor: Color = Color(0xFF0F172A),
    lightColor: Color = Color.White
) {
    // Generate a deterministic 21x21 QR matrix pattern from the input string
    val matrixSize = 21
    val grid = remember(data) {
        val hash = data.hashCode().absoluteValue
        val matrix = Array(matrixSize) { BooleanArray(matrixSize) }

        // Standard QR Finder patterns (top-left, top-right, bottom-left)
        fun placeFinderPattern(rowStart: Int, colStart: Int) {
            for (r in 0..6) {
                for (c in 0..6) {
                    val isBorder = r == 0 || r == 6 || c == 0 || c == 6
                    val isCenter = r in 2..4 && c in 2..4
                    matrix[rowStart + r][colStart + c] = isBorder || isCenter
                }
            }
        }

        placeFinderPattern(0, 0)
        placeFinderPattern(0, matrixSize - 7)
        placeFinderPattern(matrixSize - 7, 0)

        // Timing patterns
        for (i in 8 until (matrixSize - 8)) {
            matrix[6][i] = (i % 2 == 0)
            matrix[i][6] = (i % 2 == 0)
        }

        // Fill remaining payload cells deterministically with bits from data hash & chars
        var bitIndex = 0
        val dataBytes = data.toByteArray()
        for (r in 0 until matrixSize) {
            for (c in 0 until matrixSize) {
                // Skip finder patterns and separator zones
                val inTopLeft = r < 8 && c < 8
                val inTopRight = r < 8 && c >= matrixSize - 8
                val inBottomLeft = r >= matrixSize - 8 && c < 8
                val inTiming = (r == 6 && c in 8 until matrixSize - 8) || (c == 6 && r in 8 until matrixSize - 8)

                if (!inTopLeft && !inTopRight && !inBottomLeft && !inTiming) {
                    val byteVal = if (dataBytes.isNotEmpty()) dataBytes[bitIndex % dataBytes.size].toInt() else hash
                    val pseudoBit = ((byteVal xor (r * 31 + c * 17 + hash)) and (1 shl (bitIndex % 8))) != 0
                    matrix[r][c] = pseudoBit
                    bitIndex++
                }
            }
        }
        matrix
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(16.dp))
            .background(lightColor)
            .border(1.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize().padding(4.dp)) {
            val cellWidth = this.size.width / matrixSize
            val cellHeight = this.size.height / matrixSize

            for (r in 0 until matrixSize) {
                for (c in 0 until matrixSize) {
                    if (grid[r][c]) {
                        drawRect(
                            color = darkColor,
                            topLeft = Offset(c * cellWidth, r * cellHeight),
                            size = Size(cellWidth + 0.5f, cellHeight + 0.5f)
                        )
                    }
                }
            }
        }
    }
}
