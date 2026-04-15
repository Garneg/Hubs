package com.garnegsoft.hubs.ui.screens.about

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ripple
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.garnegsoft.hubs.R


@Composable
fun GithubButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.White.copy(0.1f))
            )
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1F1F1F))
            .border(width = 1.dp, color = if (MaterialTheme.colors.isLight) Color.Transparent else Color(0xFF575757), shape = RoundedCornerShape(8.dp))
//                .padding(vertical = 8.dp, horizontal = 16.dp)
            .padding(vertical = 12.dp, horizontal = 16.dp)

//                .background(),
        ,
        verticalAlignment = Alignment.CenterVertically

    ) {
        Icon(
            painter = painterResource(R.drawable.github_mark_white),
            contentDescription = null,
            tint = Color.White
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = "Код проекта на Github", color = Color.White, fontWeight = FontWeight.W600)
        Spacer(Modifier.weight(1f))
        Icon(
            painter = painterResource(R.drawable.arrow_outward),
            tint = Color.White,
            contentDescription = null
        )
    }

}