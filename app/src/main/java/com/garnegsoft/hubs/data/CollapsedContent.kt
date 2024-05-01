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
    
    var offset by mutableFloatStateOf(0f)
    
    var contentHeight by mutableFloatStateOf(0f)
    
    val isContentHidden: State<Boolean>
        get() = derivedStateOf { offset == contentHeight }
    
    suspend fun animateHide() {
        animateOffsetTo(contentHeight)
    }
    
    private suspend fun animateOffsetTo(targetValue: Float) {
        animate(
            initialValue = offset,
            targetValue = targetValue,
            animationSpec = tween(500, easing = EaseOut)
        ) { currentValue, velocity ->
            offset = currentValue
        }
    }
    
    suspend fun animateShow() {
        animateOffsetTo(0f)
    }
    
    fun show(){
        offset = 0f
    }
    
    fun hide() {
        offset = contentHeight
        
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
                if (available.y > 0 && state.offset > 0f) {
                    
                    state.offset -= available.y
                    state.offset =
                        state.offset.coerceIn(0f, state.contentHeight)
                    return available
                }
                if (available.y < 0 && state.offset < state.contentHeight) {
                    state.offset -= available.y
                    state.offset =
                        state.offset.coerceIn(0f, state.contentHeight)
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
            .padding(top = Dp(state.contentHeight / density.density) - Dp(state.offset / density.density))) {
            content()
        }
        Box(
            modifier = Modifier
                .clip(RectangleShape)
                .offset {
                    IntOffset(0, -state.offset.roundToInt())
                }
                .onGloballyPositioned {
                    state.contentHeight = it.size.height.toFloat()
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
                if (available.y > 0 && state.offset > 0f) {
                    
                    state.offset -= available.y
                    state.offset =
                        state.offset.coerceIn(0f, state.contentHeight)
                    return available
                }
                if (available.y < 0 && state.offset < state.contentHeight) {
                    state.offset -= available.y
                    state.offset =
                        state.offset.coerceIn(0f, state.contentHeight)
                    return available
                }
            }
            
            return Offset.Zero
        }
        
        override suspend fun onPreFling(available: Velocity): Velocity {
            return super.onPreFling(available)
        }
        
        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource
        ): Offset {
            Log.e("ops_consumed", consumed.toString())
            Log.e("ops_available", available.toString())
            return super.onPostScroll(consumed, available, source)
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
                
                state.offset = (state.offset - delta).coerceIn(0f, state.contentHeight)
                
                if (state.offset == 0f) {
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
            .padding(top = Dp(state.contentHeight / density.density) - Dp(state.offset / density.density))) {
            content()
        }
        Box(
            modifier = Modifier
                .clip(RectangleShape)
                .offset {
                    IntOffset(0, -state.offset.roundToInt())
                }
                .onGloballyPositioned {
                    state.contentHeight = it.size.height.toFloat()
                    collapsingContentHeightDp = (it.size.height.toFloat() / density.density)
                }
        
        ) {
            collapsingContent()
        }
    }
}
