package com.garnegsoft.hubs.ui.common.feedCards.user

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.garnegsoft.hubs.data.user.list.UserSnippet
import com.garnegsoft.hubs.data.utils.placeholderColorLegacy
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.ui.theme.DefaultRatingIndicatorColor


data class UserCardStyle(
	val backgroundColor: Color = Color.White,
	val aliasTextStyle: TextStyle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.W700),
	val avatarSize: Dp = 40.dp,
	val avatarShape: Shape = RoundedCornerShape(4.dp),
	val cardShape: Shape = RoundedCornerShape(0.dp),
	val padding: PaddingValues = PaddingValues(16.dp),
	val showSpeciality: Boolean = false,
	val specialityTextStyle: TextStyle = TextStyle(color = Color.Gray)
)

@Composable
private fun defaultUserCardStyle(): UserCardStyle {
	return UserCardStyle(
		backgroundColor = MaterialTheme.colors.surface,
		specialityTextStyle = TextStyle(color = MaterialTheme.colors.onSurface.copy(ContentAlpha.disabled))
	)
}

@Composable
fun UserCard(
	user: UserSnippet,
	modifier: Modifier = Modifier,
	style: UserCardStyle = defaultUserCardStyle(),
	indicator: @Composable () -> Unit = {
		Text(
			text = user.rating.toString(),
			fontWeight = FontWeight.W400,
			color = DefaultRatingIndicatorColor
		)
	},
	onClick: () -> Unit
) {
	Row(
		modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
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
                    .clip(shape = style.avatarShape)
                    .background(Color.White, shape = style.avatarShape),
				model = user.avatarUrl,
				contentDescription = ""
			)
		} else {
			Icon(
				modifier = Modifier
                    .size(style.avatarSize)
                    .background(Color.White, shape = style.avatarShape)
                    .border(
                        2.3.dp,
                        color = placeholderColorLegacy(user.alias),
                        shape = style.avatarShape
                    )
                    .padding(3.dp),
				painter = painterResource(id = R.drawable.user_avatar_placeholder),
				contentDescription = "",
				tint = placeholderColorLegacy(user.alias)
			)
			
		}
		Spacer(modifier = Modifier.width(style.padding.calculateStartPadding(LayoutDirection.Ltr)))
		Column(modifier = Modifier.weight(1f)) {
			
			if (style.showSpeciality && user.speciality != null) {
				Text(
					modifier = Modifier.offset(0.dp, -4.dp),
					text = user.alias,
					style = style.aliasTextStyle
				)
				user.speciality.let {
					Text(
						modifier = Modifier.offset(0.dp, -4.dp),
						text = it,
						style = style.specialityTextStyle,
						maxLines = 1,
						overflow = TextOverflow.Ellipsis
					)
				}
			} else {
				Text(
					modifier = Modifier.offset(0.dp, 0.dp),
					text = user.alias,
					style = style.aliasTextStyle
				)
			}
		}
		Box(
			modifier = Modifier
                .padding(start = 4.dp)
                .fillMaxHeight(),
			contentAlignment = Alignment.Center
		) {
			indicator()
			
		}
	}
}