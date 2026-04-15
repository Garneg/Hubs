package com.garnegsoft.hubs.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun FilterElement(
    title: String,
    onClick: () -> Unit,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Box(
        modifier = Modifier
			.fillMaxWidth()
			.height(40.dp)
			.clickable(onClick = onClick)
			.background(MaterialTheme.colors.surface)
    ) {
        Text(
            modifier = Modifier
				.align(Alignment.CenterStart)
				.padding(horizontal = 8.dp, vertical = 0.dp),
            text = title,
            style = MaterialTheme.typography.body2,
            fontWeight = FontWeight.W500,
            color = MaterialTheme.colors.onSurface.copy(0.75f)
        )
        trailingContent?.let {
            Box(modifier = Modifier.align(Alignment.CenterEnd), content = { it() })
        }
        Divider(modifier = Modifier.align(Alignment.BottomCenter))
    }
}