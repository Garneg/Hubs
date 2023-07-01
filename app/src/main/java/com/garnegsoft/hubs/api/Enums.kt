package com.garnegsoft.hubs.api

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withAnnotation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.garnegsoft.hubs.ui.theme.HubsTheme


enum class EditorVersion {
    FirstVersion,
    SecondVersion,
    Undetermined;
    companion object {
        fun fromString(version: String): EditorVersion {
            return when (version) {
                "1.0" -> FirstVersion
                "2.0" -> SecondVersion
                else -> Undetermined
            }
        }
    }
}

/**
 * Formats of article. Don't parse it on your own, use the [fromString] method
 */
enum class ArticleFormat(format: String) {
    Case("Кейс"),
    Tutorial("Туториал"),
    Roadmap("Роадмэп"),
    Retrospective("Ретроспектива"),
    Review("Обзор"),
    Opinion("Мнение"),
    FAQ("FAQ"),
    Interview("Интервью"),
    Digest("Дайджест"),
    Reportage("Репортаж"),
    Analytics("Аналитика");

    companion object {
        /**
         * Parses format of article
         */
        fun fromString(format: String): ArticleFormat? {
            return when (format.lowercase()) {
                "review" -> Review
                "digest" -> Digest
                "tutorial" -> Tutorial
                "opinion" -> Opinion
                "faq" -> FAQ
                "interview" -> Interview
                "analytics" -> Analytics
                "reportage" -> Reportage
                "case" -> Case
                "roadmap" -> Roadmap
                "retrospective" -> Retrospective
                else -> null
            }
        }
    }
}

enum class PostType {
    Article,
    News,
    Megaproject,
    Unknown;

    companion object {
        fun fromString(type: String): PostType {
            return when (type) {
                "article" -> Article
                "news" -> News
                "megaproject" -> Megaproject
                else -> Unknown
            }
        }
    }
}

enum class PostComplexity {
    Low,
    Medium,
    High,
    None;

    companion object{
        fun fromString(complexity: String?): PostComplexity {
//            if (complexity == null)
//                return PostComplexity.None
            return when(complexity){
                "low" -> Low
                "medium" -> Medium
                "high" -> High

                else -> None
            }
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Preview
@Composable
fun InlineSpoiler() {
    HubsTheme {
        val annotatedString = remember {
            buildAnnotatedString {
                append("dfjsldk dsfsdf sdf sdf dfs dfsdfs dfsdf sdf sdfs dfsdfsd fsdf sdf Что то скрывается здесь - ")
                withAnnotation("inline-spoiler", "yes"){
                    append("скрытный текст")
                }
                append("dksfjl;s fdsf dsf sdfkjfklsjd fsdfsdlkfj sdf dsklfj sdf sdkfjklsdjfljs fdklfj djkls dfsdjf sljfs")
            }
        }
        var rect: Rect? by remember { mutableStateOf(null) }
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = annotatedString, onTextLayout = {
                rect = it.getBoundingBox(66)
                Log.e("linetop", it.getLineTop(1).toString())
                Log.e("linetop", rect!!.size.height.toString())
                val height = (it.getLineBottom(1) - it.getLineTop(1)) / 32f * 20f
                val topOffset = it.getLineBottom(1) - height
                rect = Rect(
                    offset = Offset(rect!!.left, topOffset),
                    size = rect!!.size.copy(height = height)
                )
            },
                modifier = Modifier
                    .drawWithContent {
                        this.drawContent()
                        rect?.let {
                            drawRect(Color.Red.copy(0.5f), it.topLeft, it.size)
                        }
                    },
            )
        }
    }
}
