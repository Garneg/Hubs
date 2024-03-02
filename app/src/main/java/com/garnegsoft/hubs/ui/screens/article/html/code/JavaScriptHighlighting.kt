package com.garnegsoft.hubs.ui.screens.article.html.code

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle


class JavaScriptHighlighting : LanguageHighlighting() {
	val keywords = listOf(
		"await",
		"break",
		"case",
		"catch",
		"class",
		"const",
		"continue",
		"debugger",
		"default",
		"delete",
		"else",
		"export",
		"extends",
		"false",
		"finally",
		"for",
		"function",
		"if",
		"import",
		
		"instanceof",
		"new",
		"null",
		"return",
		"super",
		"switch",
		"this",
		"throw",
		"true",
		"try",
		"typeof",
		"var",
		"void",
		"while",
		"with",
		"yield",
		"in",
		"do",
	)
	val stringLiteralSpanStyle = SpanStyle(
		color = Color(0xFF579737)
	)
	val keywordSpanStyle = SpanStyle(color = Color(0xFF2F6BA7))
	val commentSpanStyle: SpanStyle = SpanStyle(
		color = Color(0xFF888888)
	)
	override fun highlight(code: String): List<AnnotatedString.Range<SpanStyle>> {
		val pipeline = CycleBasedHighlightingPipeline(code, arrayOf(
			CycleBasedComponents.StringComponent(stringLiteralSpanStyle),
			CycleBasedComponents.CharComponent(stringLiteralSpanStyle),
			CycleBasedComponents.SinglelineCommentComponent(commentSpanStyle)))
		pipeline.iterateThroughCode()
		return Defaults.highlightKeywords(code, keywords, keywordSpanStyle) + pipeline.getAnnotatedRanges()
	}
	
}