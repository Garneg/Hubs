package com.garnegsoft.hubs.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


val DefaultRatingIndicatorColor = Color(0xFFE719A9)
val HubInvestmentIndicatorColor = Color(0xFF4CBE51)

val SecondaryColor = Color(0xFF_52_64_74)
val PrimaryColor = Color(0xFFFFFFFF)
val PrimaryVariantColor = Color(0xFFF1F7FC)
val SecondaryVariantColor = Color(0xFF628DA8)

val RatingPositiveColor = Color(0xFF4CBE51)
val RatingNegativeColor = Color(0xFFC43333)

val HubSubscribedColor = Color(0xE351A843)
val TranslationLabelColor = Color(0xFF229CE6)

val DeleteButtonColor
	@Composable
	get() = themedColor(lightVariant = Color(0xFFC43333), darkVariant = Color(0xFFC43333))

@Composable
fun themedColor(lightVariant: Color, darkVariant: Color): Color =
	if (MaterialTheme.colors.isLight) lightVariant else darkVariant

