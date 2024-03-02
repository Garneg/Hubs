package com.garnegsoft.hubs.ui.screens.article.html.code

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import kotlin.reflect.KMutableProperty0


class PythonHighlighting : LanguageHighlighting() {
	
	val keywords = listOf(
		"async", "False", "await", "else", "import", "pass",
		"None", "break", "except", "in", "raise",
		"True", "class", "finally", "is", "return",
		"and", "continue", "for", "lambda", "try",
		"assert", "as", "def", "from", "nonlocal", "while",
		"del", "global", "not", "with",
		"elif", "if", "or", "yield",
	)
	
	val keywordSpanStyle = SpanStyle(color = Color(0xFF2F6BA7))
	
	val stringLiteralSpanStyle = SpanStyle(
		color = Color(0xFF579737)
	)
	
	override fun highlight(code: String): List<AnnotatedString.Range<SpanStyle>> {
		return Defaults.highlightKeywords(
			code,
			keywords,
			keywordSpanStyle
		) + CycleBasedHighlightingPipeline(
			code,
			arrayOf(
				CycleBasedComponents.CharComponent(stringLiteralSpanStyle),
				CycleBasedComponents.StringComponent(stringLiteralSpanStyle),
				PythonLineCommentComponent
			)
		).apply {
			iterateThroughCode()
		}.getAnnotatedRanges()
	}
	
	object PythonLineCommentComponent : CycleBasedComponent() {
		override val spanStyle: SpanStyle = SpanStyle(
			color = Color(0xFF888888)
		)
		var lineCommentStartIndex = -1
		override fun cycle(index: Int, code: String, lock: KMutableProperty0<Lock>) {
			if (code[index] == '#' && lock.get() == Lock.None) {
				lineCommentStartIndex = index - 1
				lock.set(Lock.SingleLineComment)
			}
			if (code[index] == '\n' && lock.get() == Lock.SingleLineComment) {
				ranges.add(AnnotatedString.Range(spanStyle, lineCommentStartIndex, index))
				lineCommentStartIndex = -1
				lock.set(Lock.None)
			}
			if (index == code.lastIndex && lock.get() == Lock.SingleLineComment){
				ranges.add(AnnotatedString.Range(spanStyle, lineCommentStartIndex, index + 1))
				lineCommentStartIndex = -1
				lock.set(Lock.None)
			}
		}
		
	}
	
}