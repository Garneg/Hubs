package com.garnegsoft.hubs.ui.screens.comments

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@Composable
fun CommentsThreadScreen(
	articleId: Int,
	threadId: Int,
	highlight: Int = 0
) {
	Scaffold(
		topBar = {
			TopAppBar(title = {
				Text(text = "Ветка комментария")
			})
		}
	) {
		LazyColumn(
			modifier = Modifier.padding(it)
		) {
		
		}
	}
}