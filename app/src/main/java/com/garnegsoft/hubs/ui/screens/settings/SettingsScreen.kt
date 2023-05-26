package com.garnegsoft.hubs.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.garnegsoft.hubs.ui.theme.HubsTheme

@Preview
@Composable
fun SettingsScreen() {
    HubsTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Settings") },
                    navigationIcon = {
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Go back"
                            )
                        }
                    }
                )
            },
            ) {
            Column(modifier = Modifier.padding(it)) {
                Column(modifier = Modifier
                    .padding(8.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color.White)
                    .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { }
                        .padding(14.dp)) {
                        Text("Dark theme")
                    }
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { }
                        .padding(14.dp)) {
                        Text("Dark theme")
                    }
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { }
                        .padding(14.dp)) {
                        Text("Dark theme")
                    }

                }


            }
        }
    }
}