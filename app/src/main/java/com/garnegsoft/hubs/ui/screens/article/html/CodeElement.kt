package com.garnegsoft.hubs.ui.screens.article.html

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.ui.screens.article.CODE_ALPHA_VALUE
import com.garnegsoft.hubs.ui.screens.article.html.code.CPPHighlighting
import com.garnegsoft.hubs.ui.screens.article.html.code.GolangHighlighting
import com.garnegsoft.hubs.ui.screens.article.html.code.JavaScriptHighlighting
import com.garnegsoft.hubs.ui.screens.article.html.code.KotlinHighlighting
import com.garnegsoft.hubs.ui.screens.article.html.code.PythonHighlighting
import com.garnegsoft.hubs.ui.screens.article.html.code.SqlHighlighting
import com.garnegsoft.hubs.ui.theme.HubsTheme


@Preview(name = "aboba")
@Composable
fun CodeElementPreview() {
	HubsTheme {
		Column(
			modifier = Modifier
				.verticalScroll(rememberScrollState())
				.padding(16.dp)
		) {
			CodeElement(
				code = """
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
	
	abstract fun highlight(code: String, useDarkThemeColor: Boolean = false): List<AnnotatedString.Range<SpanStyle>>
	
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

		""".trimIndent().replace("\t", "   "),
				//code = SqlHighlighting().keywords.sorted().joinToString("\n"),
				language = "Kotlin", spanStyle = SpanStyle()
			)
		}// aboba
	}
}


@Composable
fun CodeElement(
	code: String,
	language: String,
	spanStyle: SpanStyle,
) {
	var codeSpanStylesList by remember { mutableStateOf(listOf<AnnotatedString.Range<SpanStyle>>()) }
	LaunchedEffect(key1 = MaterialTheme.colors.isLight, block = {
		val spanStylesList = mutableListOf<AnnotatedString.Range<SpanStyle>>()
		val highlighting = when (language) {
			"Go" -> GolangHighlighting()
			"Python" -> PythonHighlighting()
			"C++" -> CPPHighlighting()
//			"C#" -> CPPHighlighting()
			"JavaScript" -> JavaScriptHighlighting()
			"JSON" -> JavaScriptHighlighting()
			"SQL" -> SqlHighlighting()
			"PostgreSQL" -> SqlHighlighting()
			"Kotlin" -> KotlinHighlighting()
			else -> null
		}
		highlighting?.let {
			codeSpanStylesList = it.highlight(code)
		}
	})
	val annotatedStringCode = remember(codeSpanStylesList) {
		AnnotatedString(
			text = code,
			spanStyles = codeSpanStylesList
		)
	}
	Column(Modifier.clip(RoundedCornerShape(8.dp))) {
		val jetbrainsMonoFontFamily = FontFamily(
			listOf(
				Font(R.font.jetbrains_mono_regular_nl, FontWeight.Normal),
				Font(R.font.jetbrains_mono_medium_nl, FontWeight.Medium),
				Font(
					R.font.jetbrains_mono_medium_nl_italic,
					FontWeight.Medium,
					FontStyle.Italic
				),
				Font(
					R.font.jetbrains_mono_regular_nl_italic,
					FontWeight.Normal,
					FontStyle.Italic
				)
			)
		)
		val robotoMono = FontFamily(Font(R.font.roboto_mono_variable))
		
		Surface(
			color = MaterialTheme.colors.onBackground.copy(CODE_ALPHA_VALUE),
			modifier = Modifier.fillMaxWidth()
		) {
			DisableSelection {
				Row(modifier = Modifier.padding(8.dp)) {
					Text(
						text = buildAnnotatedString { withStyle(spanStyle) { append(language) } },
						fontWeight = FontWeight.W600,
						fontFamily = FontFamily.SansSerif
					)
					Spacer(modifier = Modifier.width(6.dp))
				}
			}
			
			
		}
		val codeScrollState = rememberScrollState()
		Surface(
			color = MaterialTheme.colors.onBackground.copy(CODE_ALPHA_VALUE),
			modifier = Modifier.fillMaxWidth()
		) {
			Row() {
				Surface(
					color = MaterialTheme.colors.onBackground.copy(0f),
				) {
					val dividerColor = MaterialTheme.colors.onBackground
					Column(Modifier
						.drawBehind {
							val showDividerThreshold = 8.dp.toPx()
							drawRect(
								alpha = (codeScrollState.value / showDividerThreshold).coerceAtMost(
									1f
								) * 0.1f,
								color = dividerColor,
								topLeft = Offset(size.width - 3f, 0f),
								size = Size(3f, size.height)
							)
						}
						.padding(8.dp)
					) {
						var linesIndicator = String()
						for (i in 1..code.count { it == "\n"[0] } + 1) {
							linesIndicator += "$i\n"
						}
						linesIndicator = linesIndicator.take(linesIndicator.length - 1)
						
						DisableSelection {
							Text(
								text = buildAnnotatedString {
									withStyle(
										spanStyle.copy(
											color = MaterialTheme.colors.onBackground.copy(
												0.25f
											)
										)
									) { append(linesIndicator) }
								},
								lineHeight = 16.sp * 1.4f,
								color = MaterialTheme.colors.onBackground.copy(0.25f),
								fontFamily = jetbrainsMonoFontFamily,
								textAlign = TextAlign.End
							)
						}
					}
				}
				Row(
					modifier = Modifier
						.horizontalScroll(codeScrollState)
						.fillMaxWidth()
						.padding(8.dp)
				) {
					
					
						SelectionContainer {
							Text(
								text = annotatedStringCode,
								fontFamily = jetbrainsMonoFontFamily,
								fontWeight = FontWeight.Normal,
								lineHeight = 16.sp * 1.4f,
								color = MaterialTheme.colors.onBackground
							)
						}
					
				}
			}
		}
	}
}