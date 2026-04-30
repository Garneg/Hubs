package com.garnegsoft.hubs.api.tts

import android.os.Bundle
import androidx.compose.runtime.compositionLocalOf
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand


val LocalMediaController = compositionLocalOf<MediaController?> { error("Media controller not provided")  }

fun MediaController.loadArticle(articleId: Int) {
    sendCustomCommand(
        SessionCommand(TTSServiceCommands.ACTION_LOAD_ARTICLE, Bundle.EMPTY),
        Bundle().apply { putInt("id", articleId) }
    )
}


fun MediaMetadata.toArticleMetadata(): TTSPlayer.ArticleMetadata =
    TTSPlayer.ArticleMetadata(
        title = title.toString(),
        author = artist.toString(),
        thumbnailUri = artworkUri.toString(),
        articleId = extras?.getInt("articleId") ?: 0,
        offline = extras?.getBoolean("offline") ?: false
    )