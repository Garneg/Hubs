package com.garnegsoft.hubs.ui.screens.article.html.code

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

class KotlinHighlighting : LanguageHighlighting() {
	override fun highlight(
		code: String,
		useDarkThemeColor: Boolean
	): List<AnnotatedString.Range<SpanStyle>> {
		val keywordSpanStyle = SpanStyle(color = Color(0xFF2F6BA7))
		
		val stringLiteralSpanStyle = SpanStyle(
			color = Color(0xFF579737))
		
		val numberLiteralSpanStyle = SpanStyle(
			color = Color(0xFF3265C4),
		)
		
		val commentsSpanStyle = SpanStyle(
			color = Color.Gray,
			fontStyle = FontStyle.Italic,
			fontWeight = FontWeight.W400
		)
		
		val spanStylesList = mutableListOf<AnnotatedString.Range<SpanStyle>>()
		
		spanStylesList.addAll(
			LanguageHighlighting.Defaults
				.highlightKeywords(code, keywordsList, keywordSpanStyle)
		)
		
		val pipeline = CycleBasedHighlightingPipeline(code, arrayOf(
			CycleBasedComponents.CharComponent(stringLiteralSpanStyle),
			CycleBasedComponents.QuotationMarkStringComponent(stringLiteralSpanStyle),
			CycleBasedComponents.IntegerComponent(numberLiteralSpanStyle),
			CycleBasedComponents.SinglelineCommentComponent(commentsSpanStyle),
			CycleBasedComponents.MultilineComment(commentsSpanStyle)
		))
		
		pipeline.iterateThroughCode()
		spanStylesList.addAll(pipeline.getAnnotatedRanges())
		
		return spanStylesList
	}
	
	val keywordsList: List<String> = listOf(
		
		"break",
		"class",
		"continue",
		"do",
		"else",
		"false",
		"for",
		"fun",
		"if",
		
		"interface",
		"is",
		"null",
		"object",
		"package",
		"return",
		"super",
		"this",
		"throw",
		"true",
		"try",
		"typealias",
		"typeof",
		"when",
		"while",
		"by",
		"catch",
		"constructor",
		"delegate",
		"dynamic",
		"field",
		"file",
		"finally",
		"get",
		"import",
		"init",
		"param",
		"property",
		"receiver",
		"setparam",
		"set",
		"where",
		"actual",
		"abstract",
		"annotation",
		"companion",
		"const",
		"crossinline",
		"data",
		"enum",
		"expect",
		"external",
		"final",
		"infix",
		"inline",
		"inner",
		"internal",
		"lateinit",
		"noinline",
		"open",
		"operator",
		"out",
		"override",
		"private",
		"protected",
		"public",
		"reified",
		"sealed",
		"suspend",
		"tailrec",
		"value",
		"vararg",
		"in",
		"val",
		"var",
		"as",
		)
}