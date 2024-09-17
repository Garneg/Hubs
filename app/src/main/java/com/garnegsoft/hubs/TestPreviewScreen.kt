package com.garnegsoft.hubs

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview


@Preview
@Composable
private fun TestScreen() {
	val annotatedString = remember {
		buildAnnotatedString {
			appendInlineContent("aboba", "aboba")
		}
	}
	
//	Text(text = annotatedString,
//		inlineContent = mapOf("aboba" to InlineTextContent(Placeholder()))
//	)
}