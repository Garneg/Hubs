package com.garnegsoft.hubs.ui.common

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.Measured
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow


@Composable
fun MenuBaseContainer(
	modifier: Modifier = Modifier,
	content: @Composable ColumnScope.() -> Unit
) {
	Box(
		modifier = modifier
			.padding(4.dp)
	) {
		Surface(
			modifier = Modifier
				.shadow(4.dp, RoundedCornerShape(8.dp))
				.clip(RoundedCornerShape(8.dp))
				.background(MaterialTheme.colors.surface),
			elevation = 4.dp
		) {
			Column(
				modifier = Modifier
					.width(intrinsicSize = IntrinsicSize.Max)
					.widthIn(min = 150.dp)
					.verticalScroll(rememberScrollState()),
				content = content
			)
		}
	}
}

@Composable
fun MenuItem(
	title: String,
	icon: @Composable () -> Unit,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
) {
	Row(
		modifier = modifier
			.clickable(onClick = onClick)
			.padding(14.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		CompositionLocalProvider(
			LocalContentColor provides MaterialTheme.colors.onSurface,
			LocalContentAlpha provides 0.8f
		) {
			icon()
		}
		Spacer(modifier = Modifier.width(14.dp))
		Text(
			text = title,
			color = MaterialTheme.colors.onSurface
		)
		Spacer(modifier = Modifier.width(14.dp))
		Spacer(modifier = Modifier.weight(1f))
	}
}