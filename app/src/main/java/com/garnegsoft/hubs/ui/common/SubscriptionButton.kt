package com.garnegsoft.hubs.ui.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.garnegsoft.hubs.data.user.UserController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun SubscriptionButton(
	onClick: () -> Unit,
	subscribed: Boolean,
	
) {
	val textColor by animateColorAsState(targetValue = if (subscribed) Color.White else Color(0xFF4CB025))
	val backgroundColor by animateColorAsState(targetValue = if (subscribed) Color(0xFF4CB025) else Color.Transparent)
	Row(modifier = Modifier
		.padding(8.dp)
		.height(45.dp)
		.fillMaxWidth()
		.clip(RoundedCornerShape(10.dp))
		.background(backgroundColor)
		.border(
			width = 1.dp,
			shape = RoundedCornerShape(10.dp),
			color = if (subscribed) Color.Transparent else Color(
				0xFF4CB025
			)
		)
		.clickable(onClick = onClick),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.Center
	) {
		Icon(
			imageVector = if (subscribed) Icons.Default.Done else Icons.Default.Add,
			tint = textColor,
			contentDescription = null)
		Spacer(modifier = Modifier.width(4.dp))
		Text(
			modifier = Modifier.animateContentSize(),
			text = if (subscribed) "Вы подписаны" else "Подписаться",
			fontWeight = FontWeight.W500,
			color = textColor
		)
	}
}