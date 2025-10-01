package com.garnegsoft.hubs.ui.screens.imageViewer

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.EaseInBounce
import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateInt
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import coil.size.Size
import com.garnegsoft.hubs.api.dataStore.HubsDataStore
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import me.saket.telephoto.zoomable.rememberZoomableState
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.roundToInt


/**
 * It is full screen element that displays image that will be passed as **model**. It's intended to
 * be placed at top of navigation graph and draw it on other screens
 */
@Composable
fun ImageViewerScreenOverlay(
	state: ImageViewerState
) {
	BackHandler(state.show) {
		state.close()
	}
	val context = LocalContext.current

	val systemUiController = rememberSystemUiController()
	val systemDarkThemeEnabled = isSystemInDarkTheme()
	val isAppDarkThemeEnabled by HubsDataStore.Settings.Theme.ColorSchemeMode.getFlow(context)
		.run { HubsDataStore.Settings.Theme.ColorSchemeMode.mapValues(this) }
		.map { it == HubsDataStore.Settings.Theme.ColorSchemeMode.ColorScheme.Dark ||
				((it == HubsDataStore.Settings.Theme.ColorSchemeMode.ColorScheme.SystemDefined || it == HubsDataStore.Settings.Theme.ColorSchemeMode.ColorScheme.Undetermined) && systemDarkThemeEnabled)}
		.collectAsState(null)

	LaunchedEffect(state.show, block = {
		if (state.show) {
			systemUiController.setStatusBarColor(Color.Black.copy(0.5f))
			systemUiController.setNavigationBarColor(color = Color.Black.copy(0.5f), darkIcons = false, navigationBarContrastEnforced = false)
		} else {
			systemUiController.setStatusBarColor(Color.Transparent)
			isAppDarkThemeEnabled?.let {
				if (it) {
					systemUiController.setNavigationBarColor(
						color = Color.Transparent,
						false,
					)
				} else {
					systemUiController.setNavigationBarColor(
						color = Color.Transparent,
						darkIcons = true,
						navigationBarContrastEnforced = true
					)
				}
			}
		}
	})

	val splineBasedDecay = rememberSplineBasedDecay<Float>()
	var viewportHeight by remember { mutableStateOf(0) }

	AnimatedVisibility(
		modifier = Modifier.fillMaxSize(),
		visible = state.show,
		enter = scaleIn(animationSpec = tween(250, easing = EaseOutQuart), initialScale = 0.25f) + fadeIn(tween(250)),
		exit = scaleOut(targetScale = 0.9f)
		+ fadeOut(animationSpec = tween(250)),

	) {
		var offset by remember {
			mutableStateOf(0f)
		}

		val draggableState = rememberDraggableState {
			offset += it * 0.75f
		}

		val backgroundAlpha by remember {
			derivedStateOf { 1f - abs(offset) / viewportHeight }
		}
		var isDragging by remember { mutableStateOf(false) }

		val zoomableState = rememberZoomableState(
			autoApplyTransformations = false,
			zoomSpec = ZoomSpec(maxZoomFactor = 3f, preventOverOrUnderZoom = false)
		)


		val zoomableImageState = rememberZoomableImageState(zoomableState = zoomableState)

		val animatedOffset by animateFloatAsState(
			targetValue = if (isDragging) offset else 0f
		)
		val animatedCornerRadius by transition.animateDp {
			when(it) {
				EnterExitState.Visible -> 0.dp
				else -> 20.dp
			}
		}
		Box(modifier = Modifier
			.clip(RoundedCornerShape(size = animatedCornerRadius))
			.fillMaxSize()
			.onGloballyPositioned {
				viewportHeight = it.size.height
			}
			.draggable(
				state = draggableState,
				orientation = Orientation.Vertical,
				enabled = zoomableState.zoomFraction?.let { it < 0.1f } ?: true,
				onDragStarted = {
					isDragging = true
				},
				onDragStopped = {
					if (it.absoluteValue > 5500f || offset.absoluteValue > viewportHeight / 5) {
						launch {
							var lastValue = 0f
							AnimationState(
								initialValue = 0f,
								initialVelocity = it * 1.2f
							).animateDecay(splineBasedDecay) {
								offset += value - lastValue
								lastValue = value
							}
						}
						launch {
							delay(50)
							state.close()
						}
					} else {
						isDragging = false
						offset = 0f
					}
				}
			)
			.drawBehind {
				drawRect(
					color = Color.Black,
					alpha = backgroundAlpha
				)
			}
		) {


			ZoomableAsyncImage(
				modifier = Modifier
					.fillMaxSize()
					.offset { IntOffset(0, (if (isDragging) offset else animatedOffset).roundToInt()) },
				state = zoomableImageState,
				model = ImageRequest.Builder(LocalContext.current)
					.data(state.imageModel)
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
					.statusBarsPadding()
					.height(55.dp)
					.padding(horizontal = 4.dp),
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.End
			) {

				Box(
					modifier = Modifier
						.size(48.dp)
						.clip(CircleShape)
						.clickable(onClick = state::close, interactionSource = null, indication = ripple())
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
