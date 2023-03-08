package com.garnegsoft.hubs.api.utils

import androidx.compose.ui.graphics.Color

private val colors = listOf<Color>(
    Color(0xFF_8baab5),
    Color(0xFF_b58ba9),
    Color(0xFF_B18ED1),
    Color(0xFF_8bb58c)
)

fun placeholderColor(alias: String): Color {
    var counter = 0
    alias.forEach {
        counter += it.code
    }
    return colors[counter % 4]
}