package com.garnegsoft.hubs.ui.screens.subscriptions

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.garnegsoft.hubs.ui.theme.HubSubscribedColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun SubscriptionsCardsButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    subscribed: Boolean,
) {
    var buttonClickable by remember {
        mutableStateOf(true)
    }
    val coroutineScope = rememberCoroutineScope()
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(animateColorAsState(if (subscribed) HubSubscribedColor else Color.Transparent).value)
            .border(
                width = animateDpAsState(if (subscribed) 0.dp else 1.dp).value,
                color = HubSubscribedColor,
                shape = RoundedCornerShape(10.dp)
            )
            .size(40.dp)
            .clickable(onClick = {
                onClick()
                buttonClickable = false
                coroutineScope.launch {
                    delay(500)
                    buttonClickable = true
                }
            }, enabled = buttonClickable),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = subscribed
        ) {
            if (it) {
                Icon(
                    imageVector = Icons.Default.Done,
                    tint = Color.White,
                    contentDescription = null
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Add,
                    tint = HubSubscribedColor,
                    contentDescription = null
                )
            }
        }

    }
}