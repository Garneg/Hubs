package com.garnegsoft.hubs.ui.screens.article

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.LinearGradient
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.ui.theme.HubsTheme
import kotlinx.coroutines.delay
import kotlin.math.floor
import kotlin.random.Random


@Preview
@Composable
private fun ArticleScreenPlaceholderPreview() {
    HubsTheme {
        var hideContent by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            while (true) {
                delay(10000)
                hideContent = !hideContent
            }
        }
        RevealContainer(
            hideContent = hideContent,
            overlappingContent = { GenericSkeleton(seed = 5) }
        ) {
            RandomContent()
        }

    }
}


@Composable
fun RevealContainer(
    hideContent: Boolean,
    modifier: Modifier = Modifier,
    revealingAnimationSpec: FiniteAnimationSpec<Float> = tween(
        durationMillis = 1000,
        easing = EaseOutQuart
    ),
    overlappingContent: @Composable () -> Unit,
    mainContent: @Composable () -> Unit
) {
    Box(
        modifier = modifier
    ) {
        val visibilityTransition = updateTransition(hideContent)

        val animProgress by visibilityTransition.animateFloat(transitionSpec = { revealingAnimationSpec }) { if (it) 0f else 2f }


        val gradient by remember {
            derivedStateOf {
                Brush.linearGradient(
                    0f to Color.Transparent,
                    (animProgress - 0.5f).coerceAtLeast(0f) to Color.Transparent,
                    animProgress.coerceAtMost(1f) to Color.Black,
                    1f to Color.Black,
                    start = Offset.Zero,
                    end = Offset.Infinite,
                    tileMode = TileMode.Clamp
                )
            }
        }

        mainContent()
        if (visibilityTransition.isRunning || visibilityTransition.currentState ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                    .drawWithCache {
                        onDrawWithContent {
                            drawContent()        // dst
                            drawRect(            // src
                                brush = gradient,
                                size = size,
                                blendMode = BlendMode.DstIn
                            )
                        }
                    },
            ) { overlappingContent() }
        }
    }
}

@Composable
private fun RandomContent(modifier: Modifier = Modifier) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Title1", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
        Text(
            LoremIpsum(500).values.joinToString(" "),
            color = MaterialTheme.colors.onBackground.copy(0.7f)
        )
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {}, elevation = null, shape = CircleShape
        ) {
            Text("C'mon man, just click me!")
        }
    }
}


@Composable
fun GenericSkeleton(modifier: Modifier = Modifier, seed: Int) {
    val colors = MaterialTheme.colors
    Box(
        modifier = modifier
            .drawWithCache {

                val skeletonLineHeightPx = 20 * density
                val spaceBetweenLinesPx = 16 * density
                val numberOfLines =
                    floor(size.height / (skeletonLineHeightPx + spaceBetweenLinesPx))
                val random = Random(seed)
                val linesWidthList = buildList<Float> {
                    repeat(numberOfLines.toInt()) {
                        add(random.nextFloat())
                    }
                }


                onDrawBehind {
                    drawRect(color = colors.surface, size = size)

                    inset(16 * density) {
                        linesWidthList.forEachIndexed() { index, widthFactor ->
                            drawRoundRect(
                                color = colors.onSurface.copy(0.1f),
                                cornerRadius = CornerRadius(4 * density, 4 * density),
                                size = Size(
                                    width = (size.width / 2f) + (size.width / 2 * widthFactor),
                                    height = skeletonLineHeightPx
                                ),
                                topLeft = Offset(
                                    x = 0f,
                                    y = (skeletonLineHeightPx + spaceBetweenLinesPx) * index
                                )
                            )
                        }
                    }
                }
            }

    )
}

