package com.garnegsoft.hubs.ui.screens.main

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.compose.DialogHost
import androidx.navigation.compose.DialogNavigator
import coil.compose.AsyncImage
import com.garnegsoft.hubs.ui.common.TitledColumn

@Preview
@Composable
fun FilterDialog() {
    Box() {


        Dialog(
            properties = DialogProperties(true, true),
            onDismissRequest = { Log.e("aboboa", "adlajflsd") }) {

            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp))
                    .background(Color.White).padding(16.dp)
            ) {
//                Text("abobba")
                TitledColumn(title = "Ohh, Hello Moderator!") {
                        Text("aldfjl;asd")
                    }
            }
        }
    }
}