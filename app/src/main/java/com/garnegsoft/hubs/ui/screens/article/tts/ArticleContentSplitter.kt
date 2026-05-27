package com.garnegsoft.hubs.ui.screens.article.tts

import org.jsoup.nodes.Element

class ArticleContentSplitter {

    companion object {
        fun breakIntoPieces(element: Element): List<String> {
            val pieces = mutableListOf<String>()

            var currentPiece = ""
            element.text().split(". ").forEach {
                currentPiece += it

                if (currentPiece.length > 30) {
                    pieces.add(currentPiece)
                    currentPiece = ""
                }
            }
            

            return pieces
        }
    }

}