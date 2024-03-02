package com.garnegsoft.hubs.ui.screens.article.html.code

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle


enum class Lock {
	None,
	String,
	Char,
	SingleLineComment,
	MultilineString,
	MultilineComment
}

abstract class LanguageHighlighting {
	
	abstract fun highlight(code: String): List<AnnotatedString.Range<SpanStyle>>
	
	object Defaults {
		
		fun highlightKeywords(
			code: String,
			keywordsList: List<String>,
			keywordsSpanStyle: SpanStyle
		): List<AnnotatedString.Range<SpanStyle>> {
			val spanStylesList = mutableListOf<AnnotatedString.Range<SpanStyle>>()
			
			var lastKeywordIndex = 0
			while (true) {
				val pair = code.findAnyOf(keywordsList, lastKeywordIndex)
				
				if (pair == null) break
				val indexOfLastChar = pair.first + pair.second.length - 1
				
				if ((pair.first > 0 && (code[pair.first - 1].isLetterOrDigit() || code[pair.first - 1] == '_')) || (indexOfLastChar < code.lastIndex && (code[indexOfLastChar + 1].isLetterOrDigit() || code[indexOfLastChar + 1] == '_'))) {
					lastKeywordIndex = indexOfLastChar
					continue
				}
				spanStylesList.add(
					AnnotatedString.Range(keywordsSpanStyle, pair.first, indexOfLastChar + 1)
				)
				lastKeywordIndex = indexOfLastChar
			}
			return spanStylesList
		}
	}
	
}

class CycleBasedHighlightingPipeline(
	val code: String,
	val components: Array<CycleBasedComponent>
) {
	var lock: Lock = Lock.None
	
	fun iterateThroughCode() {
		for (i in 0..code.lastIndex) {
			components.forEach { it.cycle(i, code, ::lock) }
		}
	}
	
	fun getAnnotatedRanges(): List<AnnotatedString.Range<SpanStyle>> {
		return components.map { it.ranges.toList() }.reduce { acc, ranges ->
			acc + ranges
		}
	}
}
