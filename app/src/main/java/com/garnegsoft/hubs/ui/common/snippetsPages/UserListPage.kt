package com.garnegsoft.hubs.ui.common.snippetsPages

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.garnegsoft.hubs.api.user.UsersListModel
import com.garnegsoft.hubs.api.user.list.UserSnippet
import com.garnegsoft.hubs.ui.common.feedCards.user.UserCard
import com.garnegsoft.hubs.ui.theme.DefaultRatingIndicatorColor


@Composable
fun UsersListPage(
	listModel: UsersListModel,
	lazyListState: LazyListState = rememberLazyListState(),
	filter: (@Composable () -> Unit)? = null,
	cardIndicator: @Composable (UserSnippet) -> Unit = {
		Row {
			Icon(modifier = Modifier.size(18.dp).rotate(-90f),
				imageVector = Icons.Sharp.ArrowForward,
				tint = DefaultRatingIndicatorColor,
				contentDescription = null)
			Spacer(modifier = Modifier.width(2.dp))
			Text(
				text = it.rating.toString(),
				fontWeight = FontWeight.W400,
				color = DefaultRatingIndicatorColor
			)
		}
	},
	onUserClick: (alias: String) -> Unit
) {
	CommonPage(
		listModel = listModel,
		lazyListState = lazyListState,
		collapsingBar = filter
	) {
		UserCard(
			user = it,
			onClick = { onUserClick(it.alias) },
			indicator = { cardIndicator(it) }
		)
	}
}