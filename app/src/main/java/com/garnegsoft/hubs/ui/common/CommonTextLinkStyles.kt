package com.garnegsoft.hubs.ui.common

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.style.TextGeometricTransform
import com.garnegsoft.hubs.ui.theme.TextLinkColor

private val basicSpanStyle = SpanStyle(
    color = TextLinkColor,
)

@Composable
fun commonTextLinkStyles(): TextLinkStyles {
    val pressedStyleBackgroundColor = MaterialTheme.colors.onSurface
    return remember {
        TextLinkStyles(
            style = basicSpanStyle,
            pressedStyle = basicSpanStyle.copy(background = pressedStyleBackgroundColor.copy(0.1f))
            )
    }
}