package com.garnegsoft.hubs.ui.screens.article.html.code

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import kotlin.reflect.KMutableProperty0


class GolangHighlighting : LanguageHighlighting() {
	val keywordSpanStyle = SpanStyle(color = Color(0xFF2F6BA7))
	
	val keywordsList = listOf(
		"any", "break", "default", "func",
		"interface", "select", "case", "defer",
		"map", "struct", "chan", "else", "goto", "go",
		"package", "switch", "const", "fallthrough", "if",
		"range", "type", "continue", "for", "import",
		"return", "var", "nil",
		
		//base types
		"uint8", "uint16", "uint32", "uint64",
		"int8", "int16", "int32", "int64",
		"uintptr", "uint", "int",
		"float32", "float64",
		"complex64", "complex128",
		"string", "byte", "rune", "bool", "error"
	)
	
	val commentsSpanStyle = SpanStyle(
		color = Color.Gray,
		fontStyle = FontStyle.Italic
	)
	
	val functionCallSpanStyle = SpanStyle(
		color = Color(0xFF418071)
	)
	
	val numberLiteralSpanStyle = SpanStyle(
		color = Color(0xFF3265C4),
	)
	
	val builtInFunctions = listOf(
		"len", "append", "println", "print", "Error",
	)
	
	
	
	val stringLiteralSpanStyle = SpanStyle(
		color = Color(0xFF579737))
	
	override fun highlight(code: String): List<AnnotatedString.Range<SpanStyle>> {
		val spanStylesList = mutableListOf<AnnotatedString.Range<SpanStyle>>()
		
		spanStylesList.addAll(LanguageHighlighting.Defaults
			.highlightKeywords(code, keywordsList, keywordSpanStyle))
			
		val pipeline = CycleBasedHighlightingPipeline(code, arrayOf(
			GolangFunctionCallComponent(functionCallSpanStyle),
			GolangMultilineStringComponent(stringLiteralSpanStyle),
			CycleBasedComponents.CharComponent(stringLiteralSpanStyle),
			CycleBasedComponents.StringComponent(stringLiteralSpanStyle),
			CycleBasedComponents.IntegerComponent(numberLiteralSpanStyle),
			CycleBasedComponents.SinglelineCommentComponent(commentsSpanStyle),
			CycleBasedComponents.MultilineComment(commentsSpanStyle)
		))
		pipeline.iterateThroughCode()
		
		
		return spanStylesList + pipeline.getAnnotatedRanges()
	}
	
	private class GolangMultilineStringComponent(
		override val spanStyle: SpanStyle
	) : CycleBasedComponent() {
		var multilineStringStartIndex = -1
		override fun cycle(index: Int, code: String, lock: KMutableProperty0<Lock>) {
			if (code[index] == '`') {
				if (lock.get() == Lock.None) {
					multilineStringStartIndex = index
					lock.set(Lock.MultilineString)
				} else if (lock.get() == Lock.MultilineString) {
					ranges.add(
						AnnotatedString.Range(
							spanStyle,
							multilineStringStartIndex,
							index + 1
						)
					)
					lock.set(Lock.None)
					multilineStringStartIndex = -1
				}
			}
		}
	}
	
	private class GolangFunctionCallComponent(
		override val spanStyle: SpanStyle
	) : CycleBasedComponent() {
		
		val excludeFromFunctionsCalls = listOf(
			"func", "import", "var"
		)
		
		override fun cycle(index: Int, code: String, lock: KMutableProperty0<Lock>) {
			if (code[index] == '(' && lock.get() == Lock.None) {
				if (index > 0 && code[index - 1].isLetterOrDigit() || code[index - 1] == '_'){
					val functionName = code.slice(0..index - 1).trimEnd().takeLastWhile {
						it.isLetterOrDigit() || it == '_'
					}
					if (!excludeFromFunctionsCalls.contains(functionName)) {
						ranges.add(
							AnnotatedString.Range(
								spanStyle,
								index - functionName.length,
								index
							)
						)
					}
				}
			}
		}
	}
}




