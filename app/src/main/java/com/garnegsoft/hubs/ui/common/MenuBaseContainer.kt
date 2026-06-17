package com.garnegsoft.hubs.ui.common

import android.view.MenuItem
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.EnabledModifier


@Composable
fun BaseMenuContainer(
	modifier: Modifier = Modifier,
	shape: Shape = RoundedCornerShape(8.dp),
	content: @Composable ColumnScope.() -> Unit
) {
	Box(
		modifier = modifier
			.padding(8.dp)
	) {
		Surface(
			modifier = Modifier
				.shadow(4.dp, RoundedCornerShape(8.dp))
				.background(MaterialTheme.colors.surface),
			elevation = 4.dp,
			shape = shape,
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
	MenuItem(
		modifier = modifier,
		icon = icon,
		onClick = onClick,
	) {
		Text(
			text = title,
			color = MaterialTheme.colors.onSurface
		)
	}
}


@Composable
fun MenuItem(
	modifier: Modifier = Modifier,
	onClick: () -> Unit,
	enabled: Boolean = true,
	icon: (@Composable () -> Unit)? = null,
	trailingIcon: (@Composable () -> Unit)? = null,
	content: @Composable () -> Unit
) {
	Row(
		modifier = modifier
			.clickable(onClick = onClick, enabled = enabled)
			.padding(vertical = 14.dp, horizontal = 18.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		CompositionLocalProvider(
			LocalContentColor provides MaterialTheme.colors.onSurface,
			LocalContentAlpha provides 0.8f
		) {
			icon?.let {
				it()
				Spacer(modifier = Modifier.width(18.dp))
			}
		}
		content()
		Spacer(modifier = Modifier.weight(1f))
		Spacer(modifier = Modifier.width(18.dp))
		CompositionLocalProvider(
			LocalContentColor provides MaterialTheme.colors.onSurface,
			LocalContentAlpha provides 0.8f
		) {
			trailingIcon?.invoke()
		}
	}
}