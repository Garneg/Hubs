package com.garnegsoft.hubs.ui.common

import android.content.res.Resources
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.garnegsoft.hubs.api.user.list.UserSnippet
import com.garnegsoft.hubs.api.utils.placeholderColor
import com.garnegsoft.hubs.R


data class UserCardStyle(
    val backgroundColor: Color = Color.White,
    val aliasTextStyle: TextStyle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.W600),
    val avatarSize: Dp = 40.dp,
    val avatarShape: Shape = RoundedCornerShape(10.dp),
    val cardShape: Shape = RoundedCornerShape(26.dp),
    val padding: PaddingValues = PaddingValues(16.dp),
    val showSpeciality: Boolean = true,
    val specialityTextStyle: TextStyle = TextStyle(color = Color.Gray)
)

@Composable
fun UserCard(
    user: UserSnippet,
    style: UserCardStyle = UserCardStyle(),
    indicator: @Composable () -> Unit = {
        Text(text = user.rating.toString(), fontWeight = FontWeight.W400, color = Color(0xFFF555D7))
    },
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = style.cardShape)
            .background(color = style.backgroundColor)
            .clickable { onClick() }
            .padding(style.padding),
        verticalAlignment = if (style.showSpeciality && user.speciality != null) Alignment.Top else Alignment.CenterVertically
    ) {
        if (user.avatarUrl != null) {
            AsyncImage(
                modifier = Modifier
                    .size(size = style.avatarSize)
                    .clip(shape = style.avatarShape),
                model = user.avatarUrl,
                contentDescription = ""
            )
        } else {
            Icon(
                modifier = Modifier
                    .size(style.avatarSize)
                    .background(Color.White)
                    .border(2.3.dp, color = placeholderColor(user.alias), shape = style.avatarShape)
                    .padding(3.dp),
                painter = painterResource(id = R.drawable.user_avatar_placeholder),
                contentDescription = "",
                tint = placeholderColor(user.alias)
            )

        }
        Spacer(modifier = Modifier.width(style.padding.calculateStartPadding(LayoutDirection.Ltr)))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                modifier = Modifier.offset(0.dp, -4.dp),
                text = user.alias,
                style = style.aliasTextStyle
            )
            if (style.showSpeciality && user.speciality != null) {
                user.speciality.let {
                    Text(
                        modifier = Modifier.offset(0.dp, -4.dp),
                        text = it,
                        style = style.specialityTextStyle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        indicator()
    }
}