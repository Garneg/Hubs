package com.garnegsoft.hubs.ui.common.snippetsPages

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import com.garnegsoft.hubs.data.CollapsingContentState
import com.garnegsoft.hubs.data.company.CompaniesListModel
import com.garnegsoft.hubs.data.company.list.CompanySnippet
import com.garnegsoft.hubs.data.rememberCollapsingContentState
import com.garnegsoft.hubs.ui.common.feedCards.company.CompanyCard
import com.garnegsoft.hubs.ui.common.feedCards.company.DefaultCompanyIndicator


@Composable
fun CompaniesListPage(
	listModel: CompaniesListModel,
	lazyListState: LazyListState = rememberLazyListState(),
	collapsingBar: (@Composable () -> Unit)? = null,
	doInitialLoading: Boolean = true,
	collapsingContentState: CollapsingContentState = rememberCollapsingContentState(),
	onCompanyClick: (alias: String) -> Unit,
	cardIndicator: @Composable (CompanySnippet) -> Unit = { DefaultCompanyIndicator(company = it) }
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