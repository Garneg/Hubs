package com.garnegsoft.hubs.ui.screens.article

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.webkit.WebView
import android.widget.EditText
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.ImageLoader
import coil.compose.AsyncImagePainter
import com.garnegsoft.hubs.BuildConfig
import com.garnegsoft.hubs.api.AsyncGifImage
import com.garnegsoft.hubs.ui.common.AsyncSvgImage
import com.garnegsoft.hubs.ui.theme.SecondaryColor
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode

val STRONG_FONT_WEIGHT = FontWeight.W600
val HEADER_FONT_WEIGHT = FontWeight.W700
val LINE_HEIGHT_FACTOR = 1.5F

fun parseElement(
	html: String,
	spanStyle: SpanStyle,
	onViewImageRequest: ((imageUrl: String) -> Unit)? = null
): Pair<AnnotatedString?, (@Composable (SpanStyle, ElementSettings) -> Unit)?> =
	parseElement(Jsoup.parse(html), spanStyle, onViewImageRequest)

@Stable
@Composable
fun RenderHtml(
	html: String,
	spanStyle: SpanStyle =
		SpanStyle(
			color = MaterialTheme.colors.onSurface,
			fontSize = MaterialTheme.typography.body1.fontSize
		),
	elementSettings: ElementSettings
) {
	val result = remember {
		parseElement(
			html = html,
			spanStyle = spanStyle
		)
	}
	val context = LocalContext.current
	Column {
		result.first?.let { text ->
			ClickableText(
				text = text,
				style = LocalTextStyle.current.copy(lineHeight = LocalTextStyle.current.fontSize * 1.5f),
				onClick = {
					text.getStringAnnotations(it, it)
						.find { it.tag == "url" }
						?.let {
							if (it.item.startsWith("http")) {
								handleUrl(context, it.item)
								
							}
						}
				}
				)
		}
		result.second?.invoke(spanStyle, elementSettings)
	}
	
}

/**
 * WARNING! Specify fontSize with **spanStyle** or you will get exception
 */
fun parseElement(
	element: Element,
	spanStyle: SpanStyle,
	onViewImageRequest: ((imageUrl: String) -> Unit)? = null,
): Pair<AnnotatedString?, (@Composable (SpanStyle, ElementSettings) -> Unit)?> {
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
			return buildAnnotatedString { append("\n") } to null
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
		
		"summary" -> return buildAnnotatedString { } to null
	}
	
	// Child elements parsing and styling
	var childrenElementsResult: ArrayList<Pair<AnnotatedString?, (@Composable (SpanStyle, ElementSettings) -> Unit)?>> =
		ArrayList()
	element.children().forEach {
		childrenElementsResult.add(parseElement(it, ChildrenSpanStyle, onViewImageRequest))
	}
	var mainComposable: (@Composable (SpanStyle, ElementSettings) -> Unit)? = null
	
	var childrenComposables: ArrayList<@Composable (SpanStyle, ElementSettings) -> Unit> =
		ArrayList()
	
	
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
								if (thisNode.outerHtml().startsWith("\n ")
								)
									thisNode.text().drop(1)
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
								),
								color = localSpanStyle.color
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
							}
						)
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
				val url = if (element.hasClass("mention")){
					"https://habr.com${element.attr("href")}"
				} else {
					element.attr("href")
				}
				
				var urlAnnotationId = pushStringAnnotation("url", url)
				append(currentText)
				pop(urlAnnotationId)
			}
		} else {
			resultAnnotatedString += currentText
		}
	}
	
	if (!currentText.text.isBlank() && isBlock)
		
		childrenComposables.add { localSpanStyle, settings ->
			//Text(text = currentText)
			val context = LocalContext.current
			val style = TextStyle(
				fontSize = localSpanStyle.fontSize,
				lineHeight = localSpanStyle.fontSize.times(
					LINE_HEIGHT_FACTOR
				),
				color = localSpanStyle.color
			)
			val focusManager = LocalFocusManager.current
			ClickableText(
				text = currentText,
				style = style,
				onClick = {
					focusManager.clearFocus(true)
					currentText.getStringAnnotations(it, it).find { it.tag == "url" }?.let {
						if (it.item.startsWith("http")) {
							handleUrl(context, it.item)
						}
					}
				}
			)
		}
	
	// Fetching composable
	mainComposable = when (element.tagName()) {
		"h2" -> { localSpanStyle, settings ->
			Column(Modifier.padding(top = 4.dp, bottom = 8.dp)) {
				childrenComposables.forEach { it(localSpanStyle, settings) }
			}
		}
		
		"h3" -> { localSpanStyle, settings ->
			Column(Modifier.padding(top = 4.dp, bottom = 6.dp)) {
				childrenComposables.forEach { it(localSpanStyle, settings) }
			}
		}
		
		"h4" -> { localSpanStyle, settings ->
			Column(Modifier.padding(top = 4.dp, bottom = 4.dp)) {
				childrenComposables.forEach { it(localSpanStyle, settings) }
			}
		}
		
		"h5" -> { localSpanStyle, settings ->
			Column(Modifier.padding(top = 4.dp, bottom = 3.dp)) {
				childrenComposables.forEach { it(localSpanStyle, settings) }
			}
		}
		
		"p" -> if (element.html().isNotEmpty()) { localSpanStyle, settings ->
			Column(Modifier.padding(bottom = 16.dp)) {
				childrenComposables.forEach {
					it(localSpanStyle, settings)
				}
			}
		}
		else null
		
		"a" -> if (element.hasClass("anchor"))
			{ localSpanStyle, settings -> } else null
		
		"figcaption" -> if (element.text().isNotEmpty())
			{ localSpanStyle, settings ->
				val context = LocalContext.current
				val focusManager = LocalFocusManager.current
				ClickableText(
					modifier = Modifier
						.fillMaxWidth()
						.padding(bottom = 14.dp),
					text = currentText,
					style = LocalTextStyle.current.copy(
						lineHeight = ChildrenSpanStyle.fontSize.times(LINE_HEIGHT_FACTOR),
						textAlign = TextAlign.Center
					),
					onClick = {
						focusManager.clearFocus(true)
						currentText.getStringAnnotations(it, it).find { it.tag == "url" }?.let {
							if (it.item.startsWith("http")) {
								handleUrl(context, it.item)
							}
						}
					})
//                Column(Modifier.padding(bottom = 12.dp)) {
//                    childrenComposables.forEach { it(localSpanStyle) }
//                }
			}
		else null
		
		"img" -> if (element.hasClass("formula")) {
			{ localSpanStyle, settings ->
				Box(
					modifier = Modifier
						.height(50.dp)
						.fillMaxWidth()
				) {
					AsyncSvgImage(
						modifier = Modifier.align(Alignment.Center),
						data = element.attr("src"),
						contentScale = ContentScale.Inside
					)
				}
				
			}
		} else {
			{ it: SpanStyle, settings ->
				val sourceUrl = remember {
					if (element.hasAttr("data-src")) {
						element.attr("data-src")
					} else {
						element.attr("src")
					}
				}
				var isLoaded by rememberSaveable { mutableStateOf(false) }
				var aspectRatio by rememberSaveable {
					mutableStateOf(16f / 9f)
				}
				AsyncGifImage(
					model = sourceUrl,
					modifier = Modifier
						.padding(bottom = 8.dp)
						.clip(RoundedCornerShape(4.dp))
						.clickable(enabled = onViewImageRequest != null) {
							onViewImageRequest?.invoke(sourceUrl)
						}
						.background(
							if (!MaterialTheme.colors.isLight) MaterialTheme.colors.onBackground.copy(
								0.75f
							) else Color.Transparent
						)
						.aspectRatio(aspectRatio),
					contentScale = ContentScale.FillWidth,
					onState = {
						if (!isLoaded && it is AsyncImagePainter.State.Success) {
							isLoaded = true
							it.painter.intrinsicSize.let {
								Log.e("painter_bounds", it.toString())
								aspectRatio = it.width / it.height
							}
							
						}
					}
				)
			}
		}
		
		"div" -> if (element.hasClass("tm-iframe_temp"))
			{ localSpanStyle, settings ->
				
				AndroidView(modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(4.dp)),
					factory = {
						WebView(it).apply {
							
							this.settings.javaScriptEnabled = true
							this.settings.databaseEnabled = true
							isFocusable = true
							isLongClickable = true
							loadUrl(element.attr("data-src"))
						}
					})
			}
		else
			{ localSpanStyle, settings ->
				Column() {
					//Text(text = element.ownText())
					childrenComposables.forEach {
						it(localSpanStyle, settings)
					}
				}
			}
		
		"code" -> if (element.parent() != null && element.parent()!!
				.tagName() == "pre"
		) { localSpanStyle, settings ->
			Box(Modifier.padding(bottom = 4.dp)) {
				DisableSelection {
					Code(
						code = element.text(),
						language = LanguagesMap.getOrElse(
							element.attr("class"),
							{ element.attr("class") }),
						spanStyle = localSpanStyle,
						elementSettings = settings
					)
				}
			}
			resultAnnotatedString = buildAnnotatedString { }
		} else
			null
		
		
		"ul" ->
			if (element.parent() != null && element.parent()!!.tagName() == "li")
				{ localSpanStyle, settings ->
					TextList(
						modifier = Modifier.padding(bottom = 8.dp),
						items = childrenComposables,
						spanStyle = localSpanStyle,
						ordered = false,
						nested = true,
						elementSettings = settings
					)
				}
			else
				{ localSpanStyle, settings ->
					TextList(
						modifier = Modifier.padding(bottom = 8.dp),
						items = childrenComposables,
						spanStyle = localSpanStyle,
						ordered = false,
						elementSettings = settings
					)
				}
		
		"ol" -> if (element.hasAttr("start"))
			{ localSpanStyle, settings ->
				TextList(
					modifier = Modifier.padding(bottom = 8.dp),
					items = childrenComposables,
					spanStyle = localSpanStyle,
					ordered = true,
					startNumber = element.attr("start").toIntOrNull() ?: 1,
					elementSettings = settings
				)
			}
		else
			{ localSpanStyle, settings ->
				TextList(
					modifier = Modifier.padding(bottom = 8.dp),
					items = childrenComposables,
					spanStyle = localSpanStyle,
					ordered = true,
					elementSettings = settings
				)
			}
		
		"blockquote" -> { localSpanStyle, settings ->
			val quoteWidth = with(LocalDensity.current) { 4.dp.toPx() }
			Surface(
				color = Color.Transparent,
				modifier = Modifier
                    .padding(vertical = 8.dp)
                    .fillMaxWidth()
			) {
				val blockQuoteColor =
					if (MaterialTheme.colors.isLight) SecondaryColor else MaterialTheme.colors.onBackground.copy(
						0.75f
					)
				Column(modifier = Modifier
                    .drawWithContent {
                        drawContent()
                        drawRoundRect(
                            color = blockQuoteColor,
                            size = Size(quoteWidth, size.height),
                            cornerRadius = CornerRadius(quoteWidth / 2, quoteWidth / 2)
                        )
                        
                    }
                    .padding(start = 12.dp)) {
					childrenComposables.forEach {
						it(
							localSpanStyle.copy(fontStyle = FontStyle.Italic),
							settings
						)
					}
				}
			}
			
		}
		
		"hr" -> { localSpanStyle, settings ->
			Divider(
				thickness = 1.dp,
				modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
			)
			
		}
		
		"details" -> { localSpanStyle, settings ->
			var spoilerCaption = element.getElementsByTag("summary").first()?.text() ?: "Спойлер"
			var showDetails by rememberSaveable { mutableStateOf(false) }
			Surface(
				color = if (MaterialTheme.colors.isLight) Color(0x65EBEBEB) else Color(0x803C3C3C),
				modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(4.dp))
			) {
				Column(
					modifier = Modifier
						.animateContentSize()
				
				) {
					Row(
						modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDetails = !showDetails }
                            .padding(12.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						Icon(
							tint = Color(0xFF5587A3),
							modifier = Modifier
                                .size(18.dp)
                                .rotate(
                                    if (!showDetails) {
                                        -90f
                                    } else {
                                        0f
                                    }
                                ),
							imageVector = Icons.Outlined.ArrowDropDown, contentDescription = ""
						)
						Spacer(modifier = Modifier.width(4.dp))
						DisableSelection {
							Text(
								text = spoilerCaption,
								color = Color(0xFF5587A3),
								fontSize = localSpanStyle.fontSize
							)
						}
					}
					if (showDetails) {
						Divider()
						Column(
							modifier = Modifier.padding(
								start = 12.dp,
								end = 12.dp,
								bottom = 8.dp,
								top = 8.dp
							)
						) {
							childrenComposables.forEach { it(localSpanStyle, settings) }
						}
					}
				}
			}
			
		}
		
		"table" -> { localSpanStyle, settings ->
			val backgroundColor =
				if (MaterialTheme.colors.isLight) MaterialTheme.colors.surface else MaterialTheme.colors.background
			val textColor = MaterialTheme.colors.onBackground
			val fontSize =
				LocalDensity.current.fontScale * MaterialTheme.typography.body1.fontSize.value
			AndroidView(modifier = Modifier
//                    .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clip(RoundedCornerShape(0.dp)),
				factory = {
					WebView(it).apply {
						this.setBackgroundColor(backgroundColor.toArgb())
						this.isScrollContainer = false
						isFocusable = true
						isLongClickable = true
						isVerticalScrollBarEnabled = false
						val bodyElement = Element("body")
							.attr(
								"style",
								"color: rgb(${textColor.red * 255f}, ${textColor.green * 255f}, ${textColor.blue * 255f}); " +
//                                            "background-color: rgb(${backgroundColor.red * 255f}, ${backgroundColor.green * 255f}, ${backgroundColor.blue * 255f}); " +
									"font-size: ${fontSize}px;" +
									"margin: 0px;"
							)
							.appendChild(
								Element("style").appendText(
									"""
                                        td { 
                                            padding: 10px;
                                            border: 1px solid rgba(${textColor.red * 255f}, ${textColor.green * 255f}, ${textColor.blue * 255f}, 0.5);
                                            border-collapse: collapse;
                                        }
                                        table {
                                            border-collapse: collapse;
                                            min-width: 100%;
                                            table-layout: fixed;
                                            width: auto;
                                        }
                                    """.trimIndent()
								)
							)
							.appendChild(element)
						
						loadData(bodyElement.outerHtml(), "text/html; charset=utf-8", "utf-8")
					}
				})
			
		}
		
		else -> if (childrenComposables.size == 0) {
			null
		} else {
			{ localSpanStyle, settings ->
				Column() {
					childrenComposables.forEach { it(localSpanStyle, settings) }
				}
			}
		}
	}
	return resultAnnotatedString to mainComposable
}


val LanguagesMap = mapOf(
	"" to "Язык неизвестен",
	"plaintext" to "Текст",
	
	"1c" to "1C",
	
	"assembly" to "Assembly",
	
	"bash" to "BASH",
	
	"css" to "CSS",
	"cmake" to "CMake",
	"cpp" to "C++",
	"cs" to "C#",
	
	"dart" to "Dart",
	"delphi" to "Delphi",
	"diff" to "Diff",
	"django" to "Django",
	
	"elixir" to "Elixir",
	"erlang" to "Erlang",
	
	"fs" to "F#",
	
	"go" to "Go",
	
	"html" to "HTML",
	
	"java" to "Java",
	"javascript" to "JavaScript",
	"json" to "JSON",
	"julia" to "Julia",
	
	"kotlin" to "Kotlin",
	
	"lisp" to "Lisp",
	"lua" to "Lua",
	
	"markdown" to "Markdown",
	"matlab" to "Matlab",
	
	"nginx" to "NGINX",
	
	"objectivec" to "Objective C",
	
	"perl" to "Perl",
	"pgsql" to "pgSQL",
	"php" to "PHP",
	"powershell" to "PowerShell",
	"python" to "Python",
	
	"r" to "R",
	"ruby" to "Ruby",
	"rust" to "Rust",
	
	"swift" to "Swift",
	"sql" to "SQL",
	"scala" to "Scala",
	"smalltalk" to "Smalltalk",
	
	"typescript" to "TypeScript",
	
	"vala" to "Vala",
	"vbscript" to "Vbscript",
	"vhdl" to "VHDL",
	
	"xml" to "XML",
	
	"yaml" to "YAML",
	
	"zig" to "Zig"

)


// may be redundant
fun Element.isHabrBlock(): Boolean {
	val blocks = arrayListOf(
		"h1", "h2", "h3", "h4", "h5", "h6",
		"p", "div", "img", "table", "iframe",
		"li", "ul", "ol", "figcaption", "blockquote",
		"hr"
	)
	
	blocks.forEach {
		if (tagName() == it) return true
	}
	return false
}

@Composable
fun TextList(
	modifier: Modifier = Modifier,
	items: List<@Composable (SpanStyle, ElementSettings) -> Unit>,
	spanStyle: SpanStyle,
	elementSettings: ElementSettings,
	ordered: Boolean,
	nested: Boolean = false,
	startNumber: Int = 1
) {
	var itemNumber = startNumber
	Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
		items.forEach {
			
			Row() {
				DisableSelection {
					if (ordered) {
						Text(buildAnnotatedString { withStyle(spanStyle) { append("$itemNumber.") } })
					} else
						if (nested) {
							Text(text = "◦", fontSize = spanStyle.fontSize)
						} else {
							Text(text = "•", fontSize = spanStyle.fontSize)
						}
				}
				Spacer(modifier = Modifier.width(4.dp))
				it(spanStyle, elementSettings)
			}
			itemNumber++
		}
	}
}

const val CODE_ALPHA_VALUE = 0.035f

@Composable
fun Code(
	code: String,
	language: String,
	spanStyle: SpanStyle,
	elementSettings: ElementSettings,
	modifier: Modifier = Modifier
) {
	Column(modifier.clip(RoundedCornerShape(4.dp))) {
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
		Surface(
			color = MaterialTheme.colors.onBackground.copy(CODE_ALPHA_VALUE),
			modifier = Modifier.fillMaxWidth()
		) {
			Row() {
				Surface(
					color = MaterialTheme.colors.onBackground.copy(0f),
				) {
					Column(Modifier.padding(8.dp)) {
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
												0.5f
											)
										)
									) { append(linesIndicator) }
								},
								color = MaterialTheme.colors.onBackground.copy(0.5f),
								fontFamily = FontFamily.Monospace,
								textAlign = TextAlign.End
							)
						}
					}
				}
				Row(
					modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .fillMaxWidth()
                        .padding(8.dp)
				) {
					Text(
						text = buildAnnotatedString { withStyle(spanStyle) { append(code) } },
						
						fontFamily = FontFamily.Monospace,
					)
				}
			}
		}
	}
}

/**
 * Handles url. If url refers to habr, opens this link via app
 */
fun handleUrl(context: Context, url: String) {
	Log.e(
		"URL Clicked",
		url
	)
	val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
		if (url.startsWith("https://habr.com")){
			this.`package` = BuildConfig.APPLICATION_ID
		}
	}
	context.startActivity(intent)
}

