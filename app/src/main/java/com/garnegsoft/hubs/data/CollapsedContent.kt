package com.garnegsoft.hubs.data

import android.util.Log
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.tween
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import org.jetbrains.annotations.ApiStatus
import kotlin.math.roundToInt


class CollapsingContentState {
    
    val offset = mutableFloatStateOf(0f)
    
    val contentHeight = mutableFloatStateOf(0f)
    
    val isContentHidden: State<Boolean>
        get() = derivedStateOf { offset.floatValue == contentHeight.floatValue }
    
    suspend fun animateHide() {
        animateOffsetTo(contentHeight.floatValue)
    }
    
    private suspend fun animateOffsetTo(targetValue: Float) {
        animate(
            initialValue = offset.floatValue,
            targetValue = targetValue,
            animationSpec = tween(500, easing = EaseOut)
        ) { currentValue, velocity ->
            offset.floatValue = currentValue
        }
    }
    
    suspend fun animateShow() {
        animateOffsetTo(0f)
    }
    
    fun show(){
        offset.floatValue = 0f
    }
    
    fun hide() {
        offset.floatValue = contentHeight.floatValue
        
    }
    
}

@Composable
fun rememberCollapsingContentState(): CollapsingContentState {
    return remember {
        CollapsingContentState()
    }
}

/**
 * Collapsing content, that hides when user scrolls down and shows if user scrolls up.
 * Note, that you need scrolling container in content
 * @param collapsingContent UI, that will be hided/shown
 * @param content UI, that collapsing content will be drawn on
 */
@Composable
fun CollapsingContent(
    collapsingContent: @Composable () -> Unit,
    doCollapse: Boolean = true,
    state: CollapsingContentState = rememberCollapsingContentState(),
    content: @Composable () -> Unit
) {
    
    var collapsingContentHeightDp by rememberSaveable { mutableStateOf(0f) }
    val density = LocalDensity.current
    val collapsingContentHeightPx = rememberSaveable(collapsingContentHeightDp) {
        with(density) {
            collapsingContentHeightDp.dp.roundToPx().toFloat()
        }
    }

    var collapsingContentOffsetPx by rememberSaveable { mutableStateOf(0f) }
    
    val nestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            if (doCollapse) {
                if (available.y > 0 && state.offset.floatValue > 0f) {
                    
                    state.offset.floatValue -= available.y
                    state.offset.floatValue =
                        state.offset.floatValue.coerceIn(0f, state.contentHeight.floatValue)
                    return available
                }
                if (available.y < 0 && state.offset.floatValue < state.contentHeight.floatValue) {
                    state.offset.floatValue -= available.y
                    state.offset.floatValue =
                        state.offset.floatValue.coerceIn(0f, state.contentHeight.floatValue)
                    return available
                }
            }

            return Offset.Zero
        }
        
        
    }

    Box(
        modifier = Modifier.nestedScroll(nestedScrollConnection)
    ) {
        Box(Modifier
            .padding(top = Dp(state.contentHeight.floatValue / density.density) - Dp(state.offset.floatValue / density.density))) {
            content()
        }
        Box(
            modifier = Modifier
                .clip(RectangleShape)
                .offset {
                    IntOffset(0, -state.offset.floatValue.roundToInt())
                }
                .onGloballyPositioned {
                    state.contentHeight.floatValue = it.size.height.toFloat()
                    collapsingContentHeightDp = (it.size.height.toFloat() / density.density)
                }

        ) {
            collapsingContent()
        }
    }
}

@Composable
fun CollapsingContent2(
    collapsingContent: @Composable () -> Unit,
    doCollapse: Boolean = true,
    state: CollapsingContentState = rememberCollapsingContentState(),
    content: @Composable () -> Unit
) {
    
    var collapsingContentHeightDp by rememberSaveable { mutableStateOf(0f) }
    val density = LocalDensity.current
    val collapsingContentHeightPx = rememberSaveable(collapsingContentHeightDp) {
        with(density) {
            collapsingContentHeightDp.dp.roundToPx().toFloat()
        }
    }
    
    var collapsingContentOffsetPx by rememberSaveable { mutableStateOf(0f) }
    val decayAnimationSpec = rememberSplineBasedDecay<Float>()
    val nestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            if (doCollapse) {
                if (available.y > 0 && state.offset.floatValue > 0f) {
                    
                    state.offset.floatValue -= available.y
                    state.offset.floatValue =
                        state.offset.floatValue.coerceIn(0f, state.contentHeight.floatValue)
                    return available
                }
                if (available.y < 0 && state.offset.floatValue < state.contentHeight.floatValue) {
                    state.offset.floatValue -= available.y
                    state.offset.floatValue =
                        state.offset.floatValue.coerceIn(0f, state.contentHeight.floatValue)
                    return available
                }
            }
            
            return Offset.Zero
        }
        
        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            Log.e("opf_consumed", consumed.toString())
            Log.e("opf_available", available.toString())
            var lastValue = 0f
            var lastVelocity = 0f
            AnimationState(
                initialValue = 0f,
                initialVelocity = available.y
            ).animateDecay(decayAnimationSpec) {
                val delta = this.value - lastValue
                lastVelocity = this.velocity
                
                state.offset.floatValue = (state.offset.floatValue - delta).coerceAtLeast(0f)
                
                if (state.offset.floatValue == 0f) {
                    this.cancelAnimation()
                }
                
                lastValue = this.value
            }
            return available - Velocity(0f, lastVelocity)
        }
        
    }
    
    Box(
        modifier = Modifier.nestedScroll(nestedScrollConnection)
    ) {
        Box(Modifier
            .padding(top = Dp(state.contentHeight.floatValue / density.density) - Dp(state.offset.floatValue / density.density))) {
            content()
        }
        Box(
            modifier = Modifier
                .clip(RectangleShape)
                .offset {
                    IntOffset(0, -state.offset.floatValue.roundToInt())
                }
                .onGloballyPositioned {
                    state.contentHeight.floatValue = it.size.height.toFloat()
                    collapsingContentHeightDp = (it.size.height.toFloat() / density.density)
                }
        
        ) {
            collapsingContent()
        }
    }
}
