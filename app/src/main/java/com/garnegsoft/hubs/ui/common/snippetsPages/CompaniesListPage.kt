package com.garnegsoft.hubs.ui.common.snippetsPages

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.garnegsoft.hubs.api.CollapsingContentState
import com.garnegsoft.hubs.api.article.AbstractSnippetListModel
import com.garnegsoft.hubs.api.company.CompaniesListModel
import com.garnegsoft.hubs.api.company.list.CompanySnippet
import com.garnegsoft.hubs.api.rememberCollapsingContentState
import com.garnegsoft.hubs.ui.common.CompanyCard
import com.garnegsoft.hubs.ui.theme.DefaultRatingIndicatorColor


@Composable
fun CompaniesListPage(
	listModel: CompaniesListModel,
	lazyListState: LazyListState = rememberLazyListState(),
	collapsingBar: (@Composable () -> Unit)? = null,
	doInitialLoading: Boolean = true,
	collapsingContentState: CollapsingContentState = rememberCollapsingContentState(),
	onCompanyClick: (alias: String) -> Unit,
	cardIndicator: @Composable (CompanySnippet) -> Unit = {
		Text(
			it.statistics.rating.toString(),
			fontWeight = FontWeight.W400,
			color = DefaultRatingIndicatorColor
		)
	}
) {
	CommonPage(
		listModel = listModel,
		lazyListState = lazyListState,
		collapsingBar = collapsingBar,
		doInitialLoading = doInitialLoading,
		collapsingContentState = collapsingContentState,
	) {
		CompanyCard(
			company = it,
			indicator = {
				cardIndicator(it)
			},
			onClick = { onCompanyClick(it.alias) }
		)
	}
}