package com.garnegsoft.hubs.api.article.list

import com.garnegsoft.hubs.api.HabrSnippet
import com.garnegsoft.hubs.api.article.Article

data class Post(
	override val id: Int,
	val contentHtml: String,
	val author: Article.Author,
	val statistics: Article.Statistics,
	val hubs: List<Article.Hub>,
	val timePublished: String,
) : HabrSnippet