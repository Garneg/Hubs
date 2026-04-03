package com.garnegsoft.hubs.ui.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.garnegsoft.hubs.ui.theme.subscriptionColor


/**
 * @param throttle - whether button should be disabled or not. Note that throttling button won't change its appearance, it
 * just won't react to user interaction while this param set to **true**
 */
@Composable
fun SubscriptionButton(
    modifier: Modifier = Modifier,
    subscribed: Boolean,
    onClick: () -> Unit,
    throttle: Boolean,
) {
    val contentColor by animateColorAsState(
        if (subscribed) subscriptionColor()
        else Color.Transparent
    )
    val contentTextColor by animateColorAsState(
        if (subscribed) Color.White
        else subscriptionColor()
    )

    Row(
        modifier = modifier
            .padding(8.dp)
            .height(45.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .drawBehind {
                drawRect(color = contentColor)
            }
            .border(
                width = 1.dp,
                shape = RoundedCornerShape(10.dp),
                color = subscriptionColor()
            )
            .clickable(enabled = !throttle, onClick = onClick, indication = null, interactionSource = null),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (subscribed) Icons.Default.Done else Icons.Default.Add,
            contentDescription = null,
            tint = contentTextColor
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            modifier = Modifier.animateContentSize(),
            text = if (subscribed) "Вы подписаны" else "Подписаться",
            color = contentTextColor,
            fontWeight = FontWeight.W500
        )
    }
}

