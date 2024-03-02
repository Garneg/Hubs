package com.garnegsoft.hubs.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.navigation.NavBackStackEntry


object Transitions {
	val transitionDuration = 200
	object GenericTransitions {
		val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
//			scaleIn(tween(transitionDuration, easing = EaseIn), 0.9f) +
				fadeIn(tween(transitionDuration))
			
		}
		
		val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
			fadeOut(tween(transitionDuration, easing = EaseOut), 0.9f)
		}
		
		val popEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
			fadeIn(tween(transitionDuration), 0.8f)
		}
		val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
			fadeOut(tween(transitionDuration), 0.1f) //+ scaleOut(tween(transitionDuration), 0.9f)
		}
	}
	
	object EmptyTransitions {
		val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
			EnterTransition.None
		}
		
		val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
			ExitTransition.None
		}
	}
}