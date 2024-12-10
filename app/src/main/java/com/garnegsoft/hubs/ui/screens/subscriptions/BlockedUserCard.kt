package com.garnegsoft.hubs.ui.screens.subscriptions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.garnegsoft.hubs.api.user.list.UsersListController
import com.garnegsoft.hubs.ui.common.feedCards.user.UserCardStyle
import com.garnegsoft.hubs.ui.common.feedCards.user.defaultUserCardStyle


@Composable
fun BlockedUserCard(
    user: UsersListController.BlockedUser,
    style: UserCardStyle = defaultUserCardStyle(),
    onClick: () -> Unit,
    trailingContent: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
            .clip(shape = style.cardShape)
            .background(color = style.backgroundColor)
            .clickable(onClick = onClick)
            .padding(style.padding),
        verticalAlignment = Alignment.CenterVertically
    ) {

        AsyncImage(
            modifier = Modifier
                .size(size = style.avatarSize)
                .clip(shape = style.avatarShape)
                .background(Color.White, shape = style.avatarShape),
            model = user.avatarUrl,
            contentDescription = ""
        )

        Spacer(modifier = Modifier.width(style.padding.calculateStartPadding(LayoutDirection.Ltr)))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                modifier = Modifier.offset(0.dp, 0.dp),
                text = user.alias,
                style = style.aliasTextStyle
            )
        }

        Box(
            modifier = Modifier
                .padding(start = 4.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            trailingContent()
        }
    }
}