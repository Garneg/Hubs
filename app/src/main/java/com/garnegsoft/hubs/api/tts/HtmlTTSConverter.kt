package com.garnegsoft.hubs.api.tts

import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.w3c.dom.Text

class HtmlTTSConverter {
    fun convert(parentElement: Element): List<String> {
        if (parentElement.childrenSize() == 0) throw Exception("Parent element is empty")

        val children = parentElement.childNodes()
        return buildList {
            children.forEach {
                addAll(convertNode(it))
            }
        }
    }

    fun convertNode(node: Node): List<String> {
        if (node is Element) {
            val element = node as Element
            val chunks = buildList {
                when {
                    element.tagName() == "p" -> add(element.text())
                    element.tagName().startsWith("h") &&
                            element.tagName().length == 2 -> add(element.text())

                    element.tagName() == "ol" -> addAll(element.children().toList()
                        .filter { it is Element && it.tagName() == "li" }
                        .map { it.text() })

                    element.tagName() == "ul" -> addAll(element.children().toList()
                        .filter { it is Element && it.tagName() == "li" }
                        .map { it.text() })

                    element.tagName() == "blockquote" -> "Цитата — " + add(element.text())
                    element.tagName() == "div" &&
                            element.childrenSize() == 1 &&
                            element.child(0).className() == "table" ->
                        add("Таблица пропущена")
                    element.tagName() == "summary" -> add("Спойлер пропущен")
                    element.tagName() == "figure" && element.className() == "full-width" -> {
                        val figcapture = element.children().toList().find { it.tagName() == "figcapture" }
                        if (figcapture != null) {
                            add("Изображение с подписью:  ${figcapture.text()}")
                        } else {
                            add("Изображение пропущено")
                        }
                    }

                }
            }
            return chunks
        } else {
            val textNode = node as TextNode
            return listOf(textNode.text())
        }
    }


}