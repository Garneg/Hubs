package com.garnegsoft.hubs.api.tts

import androidx.compose.runtime.compositionLocalOf
import androidx.media3.session.MediaController


val LocalMediaController = compositionLocalOf<MediaController?> { error("Media controller not provided")  }