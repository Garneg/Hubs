package com.garnegsoft.hubs.ui.screens

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import me.saket.telephoto.zoomable.rememberZoomableState



@Composable
fun ImageViewScreen(
    model: Any,
    onBack: () -> Unit
) {
    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)){

        val zoomableState = rememberZoomableState(
            autoApplyTransformations = false,
            zoomSpec = ZoomSpec(maxZoomFactor = 20f, preventOverOrUnderZoom = false)
        )
        val state = rememberZoomableImageState(zoomableState = zoomableState)
        ZoomableAsyncImage(
            modifier = Modifier.fillMaxSize(),
            state = state,
            model =
            ImageRequest.Builder(LocalContext.current)
                .data(model)
                .decoderFactory(SvgDecoder.Factory())
                .decoderFactory(
                    if (Build.VERSION.SDK_INT >= 28) {
                        ImageDecoderDecoder.Factory()
                    } else {
                        GifDecoder.Factory()
                    }
                )
                .crossfade(true)
                .build(),
            contentDescription = null,
            clipToBounds = false
        )
        Row(modifier = Modifier
            .height(55.dp)
            .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically) {
            CompositionLocalProvider(LocalRippleTheme provides customRippleTheme) {
                Box(modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onBack)
                    .background(Color.Black.copy(0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "", tint = Color.White)
                }
            }

        }
    }
}

val customRippleTheme = object : RippleTheme {
    @Composable
    override fun defaultColor(): Color {
        return Color.White
    }

    @Composable
    override fun rippleAlpha(): RippleAlpha {
        return RippleAlpha(0.5f, 0.5f, 0.5f, 0.5f)
    }

}