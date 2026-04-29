package com.garnegsoft.hubs.api.tts

import android.os.Bundle
import androidx.compose.runtime.compositionLocalOf
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand


val LocalMediaController = compositionLocalOf<MediaController?> { error("Media controller not provided")  }

fun MediaController.loadArticle(articleId: Int) {
    sendCustomCommand(
        SessionCommand(TTSServiceCommands.ACTION_LOAD_ARTICLE, Bundle.EMPTY),
        Bundle().apply { putInt("id", articleId) }
    )
}

val MediaController.articleMetadata: TTSPlayer.ArticleMetadata
    get() = TTSPlayer.ArticleMetadata(
        title = mediaMetadata.title.toString(),
        author = mediaMetadata.author.toString(),
        thumbnailUri = mediaMetadata.artworkUri.toString(),
        articleId = mediaMetadata.extras?.getInt("articleId") ?: 0,
        offline = mediaMetadata.extras?.getBoolean("offline") ?: false
    )