package com.garnegsoft.hubs.ui.screens.main

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
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
                    fontSize = 16.sp,
                    lineHeight = 1.45.em,
                    color = MaterialTheme.colors.onSurface,
                    text = "Добавьте ссылки на Хабр в настройках приложения, чтобы они открывались в нё м автоматически\n\n" +
                            "Вы по-прежнему можете открывать ссылки, " +
                            "вставляя их в строку поиска приложения"
                )
            }
            Spacer(modifier = Modifier.height(56.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End,
            ){
                val context = LocalContext.current
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = null,
                    contentPadding = PaddingValues(vertical = 12.dp),
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
                Row {
                    TextButton(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        onClick = onNeverShowAgain,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.primary.copy(0.1f),
                            contentColor = MaterialTheme.colors.onBackground
                        ),
                    ) {
                        Text(text = "Никогда")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        onClick = onDismissRequest,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.primary.copy(0.1f),
                            contentColor = MaterialTheme.colors.onBackground
                        ),

                        ) {
                        Text(text = "Позже")
                    }
                }



            }
        }
    }
}