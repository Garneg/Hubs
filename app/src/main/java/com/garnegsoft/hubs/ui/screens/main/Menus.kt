package com.garnegsoft.hubs.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.utils.placeholderColor

@Composable
fun AuthorizedMenu(
    userAlias: String,
    avatarUrl: String?,
    onProfileClick: () -> Unit,
    onArticlesClick: () -> Unit,
    onCommentsClick: () -> Unit,
    onBookmarksClick: () -> Unit,
    onAboutClick: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    IconButton(onClick = { expanded = true }) {
        if (avatarUrl != null) {
            AsyncImage(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White),
                contentScale = ContentScale.FillBounds,
                model = avatarUrl, contentDescription = ""
            )
        } else {
            Icon(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))

                    .border(
                        width = 2.dp, color = placeholderColor(userAlias),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(1.dp)
                    .background(Color.White)
                    .padding(1.5.dp),
                painter = painterResource(id = R.drawable.user_avatar_placeholder),
                contentDescription = "",
                tint = placeholderColor(userAlias)
            )
        }

    }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier.width(intrinsicSize = IntrinsicSize.Max)
    ) {
        MenuItem(title = userAlias, icon = {
            if (avatarUrl != null) {
                AsyncImage(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.FillBounds,
                    model = avatarUrl, contentDescription = ""
                )
            } else {
                Icon(
                    modifier = Modifier
                        .size(32.dp)
                        .border(
                            width = 2.dp, color = placeholderColor(userAlias),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(2.5.dp),
                    painter = painterResource(id = R.drawable.user_avatar_placeholder),
                    contentDescription = "",
                    tint = placeholderColor(userAlias)
                )
            }
        }, onClick = onProfileClick)
        Divider(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))

        MenuItem(title = "Статьи", icon = {
            Icon(painter = painterResource(id = R.drawable.article), contentDescription = "")
        }, onClick = onArticlesClick)

        MenuItem(title = "Комментарии", icon = {
            Icon(
                painter = painterResource(id = R.drawable.comments_icon), contentDescription = ""
            )
        }, onClick = onCommentsClick)

        MenuItem(title = "Закладки", icon = {
            Icon(painter = painterResource(id = R.drawable.bookmark), contentDescription = "")
        }, onClick = onBookmarksClick)

        Divider(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))

        MenuItem(title = "О приложении", icon = {
            Icon(imageVector = Icons.Outlined.Info, contentDescription = "")
        }, onClick = onAboutClick)
    }
}

@Composable
fun UnauthorizedMenu(
    onLoginClick: () -> Unit,
    onAboutClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    IconButton(onClick = { expanded = true }) {
        Icon(imageVector = Icons.Outlined.MoreVert, contentDescription = "menu")
    }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        modifier = Modifier.width(intrinsicSize = IntrinsicSize.Max)
    ) {
        MenuItem(title = "Войти", icon = {
            Icon(imageVector = Icons.Outlined.ExitToApp, contentDescription = "")
        }, onClick = onLoginClick)

        Divider(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))

        MenuItem(title = "О приложении", icon = {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = "",
                modifier = Modifier.size(24.dp)
            )
        }, onClick = onAboutClick)
    }
}

@Composable
fun MenuItem(
    title: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(modifier = Modifier.width(14.dp))
        Text(title)
        Spacer(modifier = Modifier.width(14.dp))
        Spacer(modifier = Modifier.weight(1f))
    }
}