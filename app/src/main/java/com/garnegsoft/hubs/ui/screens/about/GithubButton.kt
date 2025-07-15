package com.garnegsoft.hubs.ui.screens.about

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.ui.theme.HubsTheme


@Preview
@Composable
fun GithubButton(modifier: Modifier = Modifier) {
    HubsTheme {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
//                .background()
        ){
            Icon(painter = painterResource(R.drawable.github_mark_white), contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Проект на Github")
        }
    }
}