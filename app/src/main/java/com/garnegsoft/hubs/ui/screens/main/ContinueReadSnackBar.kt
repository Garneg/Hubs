package com.garnegsoft.hubs.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun ContinueReadSnackBar(
    data: SnackbarData,
) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .shadow(8.dp, shape = RoundedCornerShape(18.dp))
        .clip(RoundedCornerShape(18.dp))
        .background(if (MaterialTheme.colors.isLight) MaterialTheme.colors.surface else Color(
            0xFF414141
        )
        )
        .clickable { data.performAction() }
        .padding(12.dp)
    ) {

        Column() {
            Box(modifier = Modifier.fillMaxWidth()){
                Text("Хотите дочитать?", color = MaterialTheme.colors.onSurface.copy(ContentAlpha.medium))
                Icon(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { data.dismiss() },
                    imageVector = Icons.Default.Close, contentDescription = "")
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row() {
                Text(
                    modifier = Modifier.weight(1f),
                    text = data.message,
                    fontSize = 18.sp,
                    maxLines = 3,
                    fontWeight = FontWeight.W700,
                    color = MaterialTheme.colors.onSurface,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(13.dp))
                Column() {
                    AsyncImage(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        model = data.actionLabel,
                        contentScale = ContentScale.Crop,
                        contentDescription = ""
                    )
                }
            }
        }


    }
}