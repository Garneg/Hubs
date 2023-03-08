package com.garnegsoft.hubs.api


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
