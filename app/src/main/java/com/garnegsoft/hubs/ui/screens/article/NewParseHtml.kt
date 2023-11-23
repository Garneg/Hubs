package com.garnegsoft.hubs.ui.screens.article

import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode


/**
 * WARNING! Specify fontSize with **spanStyle** or you will get exception
 */
fun parseChildElements(
    element: Element,
    spanStyle: SpanStyle,
    onViewImageRequest: ((imageUrl: String) -> Unit)? = null,
): Pair<AnnotatedString?, List<(@Composable (SpanStyle, ElementSettings) -> Unit)?>> {
    var isBlock = element.isHabrBlock()
    var resultAnnotatedString: AnnotatedString = buildAnnotatedString { }
    var ChildrenSpanStyle = spanStyle

    // Applying Inline elements style
    when (element.tagName()) {
        "del" -> ChildrenSpanStyle = ChildrenSpanStyle.copy(
            textDecoration = TextDecoration.combine(
                listOf(
                    ChildrenSpanStyle.textDecoration ?: TextDecoration.None,
                    TextDecoration.LineThrough
                )
            )
        )
        "b" -> ChildrenSpanStyle = ChildrenSpanStyle.copy(fontWeight = STRONG_FONT_WEIGHT)
        "strong" -> ChildrenSpanStyle = ChildrenSpanStyle.copy(fontWeight = STRONG_FONT_WEIGHT)
        "i" -> ChildrenSpanStyle = ChildrenSpanStyle.copy(fontStyle = FontStyle.Italic)
        "em" -> {
            ChildrenSpanStyle = ChildrenSpanStyle.copy(fontStyle = FontStyle.Italic)
            if (element.hasClass("searched-item")) {
                ChildrenSpanStyle = ChildrenSpanStyle.copy(background = Color(101, 238, 255, 76))
            }
        }
        "code" -> {
            ChildrenSpanStyle = ChildrenSpanStyle.copy(
                fontFamily = FontFamily.Monospace,
                background = Color(138, 156, 165, 20)
            )
        }
        "u" -> {
            ChildrenSpanStyle = ChildrenSpanStyle.copy(
                textDecoration = TextDecoration.combine(
                    listOf(
                        ChildrenSpanStyle.textDecoration ?: TextDecoration.None,
                        TextDecoration.Underline
                    )
                )
            )
        }
        "s" -> {
            ChildrenSpanStyle = ChildrenSpanStyle.copy(
                textDecoration = TextDecoration.combine(
                    listOf(
                        ChildrenSpanStyle.textDecoration ?: TextDecoration.None,
                        TextDecoration.LineThrough
                    )
                )
            )
        }
        "sup" -> {
            ChildrenSpanStyle = ChildrenSpanStyle.copy(
                baselineShift = BaselineShift.Superscript,
                fontSize = (ChildrenSpanStyle.fontSize.value - 4).coerceAtLeast(1f).sp
            )
        }
        "sub" -> {
            ChildrenSpanStyle = ChildrenSpanStyle.copy(
                baselineShift = BaselineShift.Subscript,
                fontSize = (ChildrenSpanStyle.fontSize.value - 4).coerceAtLeast(1f).sp
            )
        }

        "br" -> {
            return buildAnnotatedString { append("\n") } to emptyList()
        }
        "a" -> {
            ChildrenSpanStyle = ChildrenSpanStyle.copy(color = Color(88, 132, 185, 255))
            if (element.hasClass("user_link")) {
                resultAnnotatedString = buildAnnotatedString {
                    withStyle(ChildrenSpanStyle) { append("@") }
                }
            }
        }
        "h1" -> {
            ChildrenSpanStyle = ChildrenSpanStyle.copy(
                fontSize = (ChildrenSpanStyle.fontSize.value + 4f).sp,
                fontWeight = HEADER_FONT_WEIGHT
            )
        }
        "h2" -> {
            ChildrenSpanStyle = ChildrenSpanStyle.copy(
                fontSize = (ChildrenSpanStyle.fontSize.value + 3f).sp,
                fontWeight = HEADER_FONT_WEIGHT
            )
        }
        "h3" -> {
            ChildrenSpanStyle = ChildrenSpanStyle.copy(
                fontSize = (ChildrenSpanStyle.fontSize.value + 2f).sp,
                fontWeight = HEADER_FONT_WEIGHT
            )
        }
        "h4" -> {
            ChildrenSpanStyle = ChildrenSpanStyle.copy(
                fontSize = (ChildrenSpanStyle.fontSize.value + 2f).sp,
                fontWeight = HEADER_FONT_WEIGHT
            )
        }
        "h5" -> {
            ChildrenSpanStyle = ChildrenSpanStyle.copy(
                fontSize = (ChildrenSpanStyle.fontSize.value + 1f).sp,
                fontWeight = HEADER_FONT_WEIGHT
            )
        }
        "h6" -> {
            ChildrenSpanStyle = ChildrenSpanStyle.copy(
                fontWeight = HEADER_FONT_WEIGHT
            )
        }
        "figcaption" -> {
            ChildrenSpanStyle =
                ChildrenSpanStyle.copy(
                    color = Color.Gray,
                    // TODO: Fix unspecified span style's fontSize that leads to NaN and exception
                    fontSize = (ChildrenSpanStyle.fontSize.value - 4).sp
                )

        }
        "img" -> {
            if (element.attr("inline") == "true") {
                resultAnnotatedString = buildAnnotatedString {
                    appendInlineContent("inlineImage_")
                }
            }
        }
        "summary" -> return buildAnnotatedString { } to emptyList()
    }

    // Child elements parsing and styling
    var childrenElementsResult: ArrayList<Pair<AnnotatedString?, (@Composable (SpanStyle, ElementSettings) -> Unit)?>> =
        ArrayList()
    element.children().forEach {
        childrenElementsResult.add(parseElement(it, ChildrenSpanStyle, onViewImageRequest))
    }
    var mainComposable: (@Composable (SpanStyle, ElementSettings) -> Unit)? = null

    var childrenComposables: ArrayList<@Composable (SpanStyle, ElementSettings) -> Unit> = ArrayList()


    // Text parsing and styling + validating children element
    var currentText = buildAnnotatedString { }
    var childElementsIndex = 0

    element.childNodes().forEach { thisNode ->
        if (thisNode is TextNode) {
            if (!thisNode.isBlank)
                currentText +=
                    buildAnnotatedString {
                        withStyle(ChildrenSpanStyle) {
                            append(
                                if (thisNode.previousSibling() == null ||
                                    thisNode.previousSibling() is Element &&
                                    (thisNode.previousSibling() as Element)?.tagName() == "br"
                                )
                                    thisNode.text().trimStart()
                                else
                                    thisNode.text()
                            )
                        }
                    }
        }
        if (thisNode is Element) {

            if (childrenElementsResult[childElementsIndex].first != null)
                currentText += childrenElementsResult[childElementsIndex].first!!


            if (childrenElementsResult[childElementsIndex].second != null) {
                if (currentText.isNotEmpty() && thisNode.previousElementSibling() != null
                    && thisNode.previousElementSibling()!!.tagName() != "pre"
                ) {
                    var thisElementCurrentText = currentText
                    childrenComposables.add { localSpanStyle, settings ->
                        //Text(text = thisElementCurrentText)
                        var context = LocalContext.current
                        val focusManager = LocalFocusManager.current
                        ClickableText(
                            text = thisElementCurrentText,
                            style = LocalTextStyle.current.copy(
                                lineHeight = localSpanStyle.fontSize.times(
                                    LINE_HEIGHT_FACTOR
                                )
                            ),
                            onClick = {
                                focusManager.clearFocus(true)
                                thisElementCurrentText.getStringAnnotations(it, it)
                                    .find { it.tag == "url" }
                                    ?.let {
                                        if (it.item.startsWith("http")) {
                                            handleUrl(context, it.item)
                                        }
                                    }
                            })
                    }
                }
                childrenComposables.add(childrenElementsResult[childElementsIndex].second!!)
                currentText = buildAnnotatedString { }
            }


            // if node is block element, break the currentText annotated string and place Text() Composable
            childElementsIndex++
        }


    }
    if (!currentText.text.isBlank() && !isBlock) {
        if (element.tagName() == "a") {
            resultAnnotatedString += buildAnnotatedString {
                var urlAnnotationId = pushStringAnnotation("url", element.attr("href"))
                append(currentText)
                pop(urlAnnotationId)
            }
        } else
            resultAnnotatedString += currentText
    }

    if (!currentText.text.isBlank() && isBlock)

        childrenComposables.add { localSpanStyle, settings ->
            val context = LocalContext.current
            val focusManager = LocalFocusManager.current
            ClickableText(
                text = currentText,
                style = LocalTextStyle.current.copy(
                    lineHeight = localSpanStyle.fontSize.times(
                        LINE_HEIGHT_FACTOR
                    )
                ),
                onClick = {
                    focusManager.clearFocus(true)
                    currentText.getStringAnnotations(it, it).find { it.tag == "url" }?.let {
                        if (it.item.startsWith("http")) {
                            handleUrl(context, it.item)
    
                        }
                    }
                })
        }

    // Fetching composable

    return resultAnnotatedString to childrenComposables
}


