package com.garnegsoft.hubs.api.company

import com.garnegsoft.hubs.api.Filter
import com.garnegsoft.hubs.api.HabrList
import com.garnegsoft.hubs.api.article.AbstractSnippetListModel
import com.garnegsoft.hubs.api.company.list.CompaniesListController
import com.garnegsoft.hubs.api.company.list.CompanySnippet
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