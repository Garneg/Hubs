package com.garnegsoft.hubs.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.garnegsoft.hubs.BuildConfig
import com.garnegsoft.hubs.R

@Composable
fun AboutScreen(
    onBackClicked: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                elevation = 0.dp,
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "")
                    }
                },
                title = { Text(text = stringResource(id = R.string.about_app_screen_title))})
        }
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(it)
            .padding(12.dp)) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(modifier = Modifier.size(200.dp)) {
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .padding(30.dp)
                        .clip(RoundedCornerShape(20))
                        .background(Color(0xFF72A7D6)))
                    Icon(
                        modifier = Modifier.size(200.dp),
                        painter = painterResource(id = R.drawable.foreground_filled),
                        contentDescription = "app icon",
                        tint = Color.White
                    )
                }
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.subtitle1
                )
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(id = R.string.about_app_text) + stringResource(id = R.string.developer_email),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.onBackground.copy(.5f)
                )
                val developerEmail = stringResource(id = R.string.developer_email)
                val context = LocalContext.current
                OutlinedButton(
                    modifier = Modifier.padding(16.dp),
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                            "mailto",developerEmail, null))
                        context.startActivity(intent)
                    }
                ) {
                    Text(
                        text = stringResource(id = R.string.write_developer),
                        style = MaterialTheme.typography.body1
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(imageVector = Icons.Outlined.Email, contentDescription = "")
                }

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(id = R.string.feedback_is_important),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.onBackground.copy(.5f)
                )
            }
            Text(
                modifier = Modifier.align(Alignment.BottomCenter),
                text = "${stringResource(id = R.string.version)} ${BuildConfig.VERSION_NAME} ${BuildConfig.BUILD_TYPE}",
                color = MaterialTheme.colors.onBackground.copy(.5f),

            )
        }
    }
}
