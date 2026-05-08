package com.garnegsoft.hubs.ui.screens.article.html.parserV2

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import com.garnegsoft.hubs.ui.screens.article.ElementSettings
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node

class HtmlParserV2 {

    private var html: String = ""

    private val anchorsClickListeners: MutableList<(String) -> Unit> = mutableListOf()


    fun newBuilder(): Builder {
        return Builder(this)
    }

//    fun parseHtml(): List<Blocks> {
//
//    }

    data class Blocks(
        val htmlNode: Node,
        val content: @Composable () -> Unit
    )

    class Builder {
        var parser = HtmlParserV2()

        constructor(parser: HtmlParserV2){
            this.parser = parser
        }

        constructor()

        fun build(): HtmlParserV2 {
            if (parser.html.isBlank()) throw Exception("HtmlParser can't be empty")
            return parser
        }

        fun setHtml(html: String): Builder {
            parser.html = html
            return this
        }

        fun addAnchorClickListener(listener: (anchorUri: String) -> Unit): Builder {
            parser.anchorsClickListeners.add(listener)
            return this
        }

    }
}

fun test() {
    HtmlParserV2.Builder()
        .setHtml("")
        .build()

}