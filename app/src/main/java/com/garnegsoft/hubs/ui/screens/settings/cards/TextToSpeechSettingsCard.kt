package com.garnegsoft.hubs.ui.screens.settings.cards

import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.glance.LocalContext


@Composable
fun TextToSpeechSettingsCard(
    onVoicePickerScreen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsCard(
        title = "Озвучка статей"
    ) {
        SettingsCardItem(
            title = "Голос и движок",
            onClick = onVoicePickerScreen,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null
                )
            }
        )
    }
}