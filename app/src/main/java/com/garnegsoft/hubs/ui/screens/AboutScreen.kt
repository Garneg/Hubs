package com.garnegsoft.hubs.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.garnegsoft.hubs.BuildConfig

@Composable
fun AboutScreen(
    onBackClicked: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "")
                    }
                },
                title = { Text(text = "О приложении")})
        }
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(it)
            .padding(12.dp)) {
            Column() {

            }
            Text(
                text = "Хабы, версия ${BuildConfig.VERSION_NAME} ${BuildConfig.BUILD_TYPE}",
                modifier = Modifier.align(Alignment.BottomCenter),
                color = Color.Gray
            )
        }
    }
}
