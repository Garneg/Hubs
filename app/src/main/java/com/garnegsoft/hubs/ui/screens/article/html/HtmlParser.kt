package com.garnegsoft.hubs.ui.screens.article.html

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.garnegsoft.hubs.ui.screens.article.html.HtmlParser.Builder.Companion.newBuilder
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import kotlin.properties.Delegates


class HtmlParser private constructor(

) {
	var isOffline: Boolean = false
		private set
	var urlHandler: ((String) -> Unit)? = null
		private set
	
//	fun parse(html: Element): HtmlParserResult {
//
//	}
	
//	fun processElement(element: Element): HtmlParsedElement {
//		val children = element.children().forEach { processElement(it) }
//
//	}
	
	
	class Builder private constructor(private val parserInstance: HtmlParser){
		
		constructor() : this(HtmlParser())
		
		fun setOfflineMode(enabled: Boolean): Builder {
			parserInstance.isOffline = enabled
			return this
		}
		
		fun handleUrl(handler: (String) -> Unit): Builder {
			parserInstance.urlHandler = handler
			return this
		}
		
		fun build(): HtmlParser {
			return parserInstance
		}
		
		companion object {
			fun HtmlParser.newBuilder(): HtmlParser.Builder {
				return HtmlParser.Builder(this)
			}
		}
	}
	private class BlockElements {
		companion object {
			@Composable
			fun FullwidthImage(
				onClick: () -> Unit, modifier: Modifier = Modifier) {
				
			}
			
			
		}
	}
	
}

class HtmlParserResult(elements: List<HtmlParsedElement>)

class HtmlParsedElement(metadata: Metadata, composable: @Composable () -> Unit) {
	class Metadata(element: Element)
}

