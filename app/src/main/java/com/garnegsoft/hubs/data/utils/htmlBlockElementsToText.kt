package com.garnegsoft.hubs.data.utils

import org.jsoup.Jsoup


fun htmlBlocksToText(html: String): String {
	val stringBuilder = StringBuilder()
	Jsoup.parse(html).body().children()[0].children().forEach {
		when(it.tagName()) {
			"blockquote" ->  stringBuilder.append("*Цитата* ")
			"img" -> stringBuilder.append("*Изображение* ")
			else -> stringBuilder.append(it.text())
		}
	}
	return stringBuilder.toString()
}