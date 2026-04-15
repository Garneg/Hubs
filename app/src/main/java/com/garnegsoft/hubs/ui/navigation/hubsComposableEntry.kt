package com.garnegsoft.hubs.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.EaseOutQuint
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable


fun NavGraphBuilder.hubsComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    enterTransition:
    (@JvmSuppressWildcards
    AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? =
        null,
    exitTransition:
    (@JvmSuppressWildcards
    AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? =
        null,
    popEnterTransition:
    (@JvmSuppressWildcards
    AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? =
        enterTransition,
    popExitTransition:
    (@JvmSuppressWildcards
    AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? =
        exitTransition,
    sizeTransform:
    (@JvmSuppressWildcards
    AnimatedContentTransitionScope<NavBackStackEntry>.() -> SizeTransform?)? =
        null,
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {

    composable(
        route = route,
        arguments = arguments,
        deepLinks = deepLinks,
        enterTransition = enterTransition,
        exitTransition = exitTransition,
        popEnterTransition = popEnterTransition,
        popExitTransition = popExitTransition,
        sizeTransform = sizeTransform
    ) { navBackStackEntry ->

        val cornerRadius by transition.animateDp(
            transitionSpec = { tween(durationMillis = 150, easing = EaseOutQuint) }
        ) {
            when (it) {
                EnterExitState.PreEnter -> 24.dp
                EnterExitState.Visible -> 0.dp
                EnterExitState.PostExit -> 24.dp
            }
        }


//
//        val scrimAnimatedAlpha by animateFloatAsState(
//            if (transition.isRunning && !destinationLifecycleState.isAtLeast(Lifecycle.State.RESUMED)) 0.25f
//            else 0f
//        )

        Box(
            modifier = Modifier
                .shadow(16.dp, shape = RoundedCornerShape(cornerRadius))
                .clip(RoundedCornerShape(cornerRadius))
//                .drawWithContent {
//                    drawContent()
//                    drawRect(color = Color.Black.copy(scrimAnimatedAlpha))
//                }
        ) {
            content(navBackStackEntry)
        }
    }
}


