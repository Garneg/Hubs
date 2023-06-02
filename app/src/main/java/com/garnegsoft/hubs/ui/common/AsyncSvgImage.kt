package com.garnegsoft.hubs.ui.common

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest

private val colorMatrix = floatArrayOf(
    -0.7f, 0f, 0f, 0f, 255f,
    0f, -0.7f, 0f, 0f, 255f,
    0f, 0f, -0.7f, 0f, 255f,
    0f, 0f, 0f, 1f, 0f
)

@Composable
fun AsyncSvgImage(
    modifier: Modifier = Modifier,
    data: Any?,
    contentScale: ContentScale,
    revertColorsOnDarkTheme: Boolean = true,
    contentDescription: String? = null
) {
    val context = LocalContext.current
    AsyncImage(
        modifier = modifier,
        model = ImageRequest.Builder(context)
            .data(data)
            .decoderFactory(SvgDecoder.Factory())
            .build(),
        contentDescription = contentDescription,
        colorFilter = if (MaterialTheme.colors.isLight || !revertColorsOnDarkTheme) null else ColorFilter
            .colorMatrix(ColorMatrix(colorMatrix)),
        contentScale = contentScale
    )
}