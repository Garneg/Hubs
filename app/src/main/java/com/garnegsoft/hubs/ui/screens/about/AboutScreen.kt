package com.garnegsoft.hubs.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "")
                    }
                },
                title = { Text(text = stringResource(id = R.string.about_app))})
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
                    fontWeight = FontWeight.W700
                )
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(id = R.string.about_app_text),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.onBackground.copy(.5f)
                )

            }
            Text(
                modifier = Modifier.align(Alignment.BottomCenter),
                text = "${stringResource(id = R.string.app_name)}, ${stringResource(id = R.string.version)} ${BuildConfig.VERSION_NAME} ${BuildConfig.BUILD_TYPE}",
                color = MaterialTheme.colors.onBackground.copy(.5f)
            )
        }
    }
}
