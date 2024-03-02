package com.garnegsoft.hubs.ui.screens.article.html.code

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.toLowerCase
import java.util.Locale


class SqlHighlighting : LanguageHighlighting() {
	val keywords = listOf(
		"ADD",
		"CONSTRAINT",
		"ALL",
		"ALTER",
		"COLUMN",
		"TABLE",
		"AND",
		"ANY",
		"ASC",
		"BACKUP",
		"BETWEEN",
		"CASE",
		"CHECK",
		"CONSTRAINT",
		"REPLACE",
		"CREATE",
		"DATABASE",
		"DEFAULT",
		"DELETE",
		"DESC",
		"DISTINCT",
		"DROP",
		"EXEC",
		"EXISTS",
		"FOREIGN",
		"KEY",
		"FROM",
		"FULL",
		"OUTER",
		"JOIN",
		"GROUP",
		"HAVING",
		"INTO",
		"INDEX",
		"INNER",
		"INSERT",
		"JOIN",
		"LEFT",
		"LIKE",
		"LIMIT",
		"NOT",
		"NOT",
		"ORDER",
		"OUTER",
		"PRIMARY",
		"PROCEDURE",
		"RIGHT",
		"SELECT",
		"SET",
		"TABLE",
		"TOP",
		"TRUNCATE",
		"UNION",
		"UNIQUE",
		"UPDATE",
		"VALUES",
		"VIEW",
		"WHERE",
		"BY",
		"IN",
		"AS",
		"IS",
		"OR",
	).map { it.lowercase() }
	val keywordSpanStyle = SpanStyle(color = Color(0xFF2F6BA7))
	override fun highlight(code: String): List<AnnotatedString.Range<SpanStyle>> {
		return Defaults.highlightKeywords(code.lowercase(), keywords, keywordSpanStyle)
	}
	
}