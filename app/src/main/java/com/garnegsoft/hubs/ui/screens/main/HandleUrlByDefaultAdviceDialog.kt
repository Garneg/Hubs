package com.garnegsoft.hubs.ui.screens.main

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import com.garnegsoft.hubs.BuildConfig
import com.garnegsoft.hubs.ui.common.BaseTitledDialog


@Composable
fun HandleUrlByDefaultAdviceDialog(
    onDismissRequest: () -> Unit,
    onNeverShowAgain: () -> Unit,
    onRedirectedToSettings: () -> Unit
) {
    BaseTitledDialog(
        onDismiss = onDismissRequest,
        title = "Настройте открытие ссылок",

    ) {

        Column(
            modifier = Modifier.height(IntrinsicSize.Min)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                Text(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    color = MaterialTheme.colors.onSurface,
                    text = "Начиная с 12-ой версии Android приложение не сможет по умолчанию открывать ссылки, " +
                            "ведущие на статьи, профили, компании, хабы и т.д. \n" +
                            "Чтобы приложение смогло снова открывать ссылки, " +
                            "предоставьте разрешение открывать ссылки по умолчанию\n\n" +
                            "Если Вы по какой-то причине не можете это сделать, " +
                            "Вы по-прежнему можете открывать ссылки, " +
                            "копируя их и вставляя в поиск в приложении"
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ){
                TextButton(
                    onClick = onNeverShowAgain
                ) {
                    Text(text = "Больше не показывать")
                }
                TextButton(
                    onClick = onDismissRequest
                ) {
                    Text(text = "Позже")
                }

                val context = LocalContext.current
                Button(
                    onClick = {

                        if (Build.VERSION.SDK_INT >= 31) {
                            val intent =
                                Intent(Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS).apply {
                                    data = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
                                }
                            context.startActivity(intent)
                        } else {
                            Toast.makeText(context, "Версия Android ниже 12", Toast.LENGTH_SHORT)
                                .show()
                        }
                        onRedirectedToSettings()
                    }
                ) {
                    Text("Открыть настройки")
                }
            }
        }
    }
}