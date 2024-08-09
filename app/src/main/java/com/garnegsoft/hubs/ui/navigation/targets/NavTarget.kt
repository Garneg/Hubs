package com.garnegsoft.hubs.ui.navigation.targets

abstract class NavTarget {
	abstract val baseRoute: String
	abstract fun toStringRoute(): String
}