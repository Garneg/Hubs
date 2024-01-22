package com.garnegsoft.hubs.ui.screens.article.html.code

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight


class CPPHighlighting : LanguageHighlighting() {
	
	val keywords = listOf(
		"alignas",
		"alignof",
		"and_eq",
		"asm",
		"auto",
		"bitand",
		"bitor",
		"bool",
		"break",
		"case",
		"catch",
		"char8_t",
		"char16_t",
		"char32_t",
		"char",
		"class",
		"compl",
		"concept",
		"const_cast",
		"consteval",
		"constexpr",
		"constinit",
		"const",
		"continue",
		"co_await",
		"co_return",
		"co_yield",
		"decltype",
		"default",
		"delete",
		"double",
		"dynamic_cast",
		"else",
		"enum",
		"explicit",
		"export",
		"extern",
		"false",
		"float",
		"for",
		"final",
		"friend",
		"goto",
		"if",
		"inline",
		"import",
		"long",
		"mutable",
		"module",
		"namespace",
		"new",
		"noexcept",
		"not_eq",
		"not",
		"nullptr",
		"operator",
		"or_eq",
		"override",
		"private",
		"protected",
		"public",
		"register",
		"reinterpret_cast",
		"requires",
		"return",
		"short",
		"signed",
		"sizeof",
		"static_cast",
		"static_assert",
		"static",
		"struct",
		"switch",
		"template",
		"this",
		"thread_local",
		"throw",
		"true",
		"try",
		"typedef",
		"typeid",
		"typename",
		"union",
		"unsigned",
		"using",
		"virtual",
		"void",
		"volatile",
		"wchar_t",
		"while",
		"xor_eq",
		"xor",
		"do",
		"or",
		"and",
		"int",
	)
	val keywordSpanStyle = SpanStyle(color = Color(0xFF2F6BA7))
	val stringLiteralSpanStyle = SpanStyle(
		color = Color(0xFF579737)
	)
	val commentsSpanStyle = SpanStyle(
		color = Color.Gray,
		fontStyle = FontStyle.Italic,
		fontWeight = FontWeight.W400
	)
	override fun highlight(code: String): List<AnnotatedString.Range<SpanStyle>> {
		return Defaults.highlightKeywords(code, keywords, keywordSpanStyle) +
			CycleBasedHighlightingPipeline(code,
				arrayOf(
					CycleBasedComponents.StringComponent(stringLiteralSpanStyle),
					CycleBasedComponents.SinglelineCommentComponent(commentsSpanStyle),
					CycleBasedComponents.MultilineComment(commentsSpanStyle)
				)
			).apply { iterateThroughCode() }.getAnnotatedRanges()
	}
	
}