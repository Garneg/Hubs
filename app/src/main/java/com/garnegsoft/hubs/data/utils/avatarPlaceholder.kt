package com.garnegsoft.hubs.data.utils

import androidx.compose.ui.graphics.Color

private val colors = listOf<Color>(
    Color(0xFF_8baab5),
    Color(0xFF_b58ba9),
    Color(0xFF_B18ED1),
    Color(0xFF_8bb58c)
)

fun placeholderColorLegacy(alias: String): Color {
    var counter = 0
    alias.forEach {
        counter += it.code
    }
    return colors[counter % 4]
}

const val placholdersNumber = 200

fun placeholderAvatarUrl(alias: String): String {
    var result = alias.toCharArray().map { it.code }
        .reduce { acc, i ->
            acc + i
        } % placholdersNumber
    result++
    val resultString = result.toString().padStart(3, '0')
    return "https://assets.habr.com/habr-web/img/avatars/${resultString}.png"
}
