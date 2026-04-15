package com.garnegsoft.hubs.ui.common

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import com.garnegsoft.hubs.ui.theme.TextLinkColor

private val basicSpanStyle = SpanStyle(
    color = TextLinkColor,
)

@Composable
fun commonTextLinkStyles(): TextLinkStyles {
    val pressedStyleBackgroundColor = if (MaterialTheme.colors.isLight) TextLinkColor else MaterialTheme.colors.onSurface
    return remember {
        TextLinkStyles(
            style = basicSpanStyle,
            pressedStyle = basicSpanStyle.copy(background = pressedStyleBackgroundColor.copy(0.1f))
            )
    }
}