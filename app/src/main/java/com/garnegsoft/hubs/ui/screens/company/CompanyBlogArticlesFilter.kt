package com.garnegsoft.hubs.ui.screens.company

import androidx.compose.runtime.Composable
import com.garnegsoft.hubs.api.Filter


class CompanyBlogArticlesFilter(
	val showLast: Boolean
) : Filter {
	override fun toArgsMap(): Map<String, String> {
		TODO("Not yet implemented")
	}
	
	override fun getTitle(): String {
		TODO("Not yet implemented")
	}
}

@Composable
fun CompanyBlogArticlesFilter() {

}