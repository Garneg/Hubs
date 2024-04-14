package com.garnegsoft.hubs.data.company

import com.garnegsoft.hubs.data.Filter
import com.garnegsoft.hubs.data.HabrList
import com.garnegsoft.hubs.data.article.AbstractSnippetListModel
import com.garnegsoft.hubs.data.company.list.CompaniesListController
import com.garnegsoft.hubs.data.company.list.CompanySnippet
import kotlinx.coroutines.CoroutineScope

class CompaniesListModel(
	override val path: String,
	override val coroutineScope: CoroutineScope,
	vararg baseArgs: Pair<String, String>,
	initialFilter: Filter? = null,
) : AbstractSnippetListModel<CompanySnippet>(
	path = path,
	coroutineScope = coroutineScope,
	baseArgs = baseArgs.toMap(),
	initialFilter = initialFilter
) {
	override fun load(args: Map<String, String>): HabrList<CompanySnippet>? =
		CompaniesListController.get(path, args)
}