package com.garnegsoft.hubs.api.utils

import org.jsoup.Jsoup


fun htmlBlocksToText(html: String): String {
	val stringBuilder = StringBuilder()
	val document = Jsoup.parse(html)
	document.body().children()[0].children().forEach {
		when(it.tagName()) {
			"blockquote" ->  stringBuilder.append("*Цитата* ")
			"img" -> stringBuilder.append("*Изображение* ")
			else -> stringBuilder.append(it.text())
		}
	}
	if (stringBuilder.isEmpty()) {
		stringBuilder.append(document.body().text())
	}
	return stringBuilder.toString()
}