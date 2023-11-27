package com.garnegsoft.hubs.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toDrawable
import coil.decode.DataSource
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.request.ImageRequest
import coil.size.Size
import com.garnegsoft.hubs.api.CommonImageRequestFetcher
import com.garnegsoft.hubs.api.offlineResourcesDir
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import me.saket.telephoto.zoomable.rememberZoomableState
import java.io.File
import kotlin.math.absoluteValue
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ImageViewScreen(
	model: Any,
	onBack: () -> Unit
) {
	val context = LocalContext.current
	val zoomableState = rememberZoomableState(
		autoApplyTransformations = false,
		zoomSpec = ZoomSpec(maxZoomFactor = 3f, preventOverOrUnderZoom = false)
	)
	
	val state = rememberZoomableImageState(zoomableState = zoomableState)
	
	val systemUiController = rememberSystemUiController()
	val statusBarColor = MaterialTheme.colors.let {
		if (it.isLight)
			it.primary
		else
			it.surface
	}
	DisposableEffect(key1 = Unit, effect = {
		systemUiController.setStatusBarColor(Color.Black)
		onDispose {
			systemUiController.setStatusBarColor(statusBarColor)
		}
	})
	var offset by remember {
		mutableStateOf(0f)
	}
	
	val draggableState = rememberDraggableState {
		offset += it
	}
	var isDragging by remember { mutableStateOf(false) }
	val animatedOffset by animateFloatAsState(
		targetValue = if (isDragging) offset else 0f
	)
	
	
	val screenHeight = LocalConfiguration.current.screenHeightDp * LocalDensity.current.density
	val splineBasedDecay = rememberSplineBasedDecay<Float>()
	Box(modifier = Modifier
		.fillMaxSize()
		.draggable(
			state = draggableState,
			orientation = Orientation.Vertical,
			enabled = true,
			onDragStarted = {
				isDragging = true
			},
			onDragStopped = {
				if (it.absoluteValue > 8000f || offset.absoluteValue > screenHeight / 3) {
					launch {
						var lastValue = 0f
						AnimationState(
							initialValue = 0f,
							initialVelocity = it
						).animateDecay(splineBasedDecay) {
							offset += value - lastValue
							lastValue = value
						}
					}
					launch {
						delay(50)
						onBack()
					}
				} else {
					isDragging = false
					offset = 0f
				}
			}
		)
		.background(Color.Black)) {
		
		
		ZoomableAsyncImage(
			modifier = Modifier
				.fillMaxSize()
				.offset { IntOffset(0, (if (isDragging) offset else animatedOffset).roundToInt()) },
			state = state,
			model = ImageRequest.Builder(LocalContext.current)
				.data(model)
				//.decoderFactory(SvgDecoder.Factory())
				.decoderFactory(
					if (Build.VERSION.SDK_INT >= 28) {
						ImageDecoderDecoder.Factory()
					} else {
						GifDecoder.Factory()
					}
				)
				.size(Size.ORIGINAL)
				.crossfade(true)
				.build(),
			contentDescription = null,
			clipToBounds = false
		)
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.height(55.dp)
				.padding(horizontal = 4.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.End
		) {
			CompositionLocalProvider(LocalRippleTheme provides customRippleTheme) {
				Box(
					modifier = Modifier
						.size(48.dp)
						.clip(CircleShape)
						.clickable(onClick = onBack)
						.background(Color.Black.copy(0.5f)),
					contentAlignment = Alignment.Center
				) {
					Icon(
						imageVector = Icons.Default.Close,
						contentDescription = "",
						tint = Color.White
					)
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