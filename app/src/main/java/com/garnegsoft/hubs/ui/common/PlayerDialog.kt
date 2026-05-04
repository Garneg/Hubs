package com.garnegsoft.hubs.ui.common

import android.graphics.Bitmap
import android.media.session.PlaybackState
import androidx.compose.ui.graphics.Color
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.drawable.toBitmap
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.ui.compose.buttons.PlayPauseButton
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.intercept.Interceptor
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.article.Article
import com.garnegsoft.hubs.api.tts.loadArticle
import com.garnegsoft.hubs.api.tts.toArticleMetadata


@OptIn(UnstableApi::class)
@Composable
fun PlayerDialog(
    show: Boolean,
    onDismissRequest: () -> Unit,
    mediaController: MediaController?,
    onTitleClick: () -> Unit,
    onAuthorClick: () -> Unit,
    onCurrentPlayingClick: (() -> Unit)? = null,
    article: Article? = null
) {
    if (show) {
        var palette by remember { mutableStateOf<Palette?>(null) }

        var mediaMetadata by remember { mutableStateOf(mediaController?.mediaMetadata)}
        val articleMediaMetadata = remember(mediaMetadata) { mediaMetadata?.toArticleMetadata() }
        var isPlayerLoading by remember { mutableStateOf(false) }

        LaunchedEffect(mediaController) {
            mediaController?.addListener(
                object : Player.Listener {
                    override fun onMediaMetadataChanged(playerMediaMetadata: MediaMetadata) {
                        mediaMetadata = playerMediaMetadata
                        super.onMediaMetadataChanged(playerMediaMetadata)
                    }

                    override fun onIsLoadingChanged(isLoading: Boolean) {
                        if (isPlayerLoading && isLoading == false) {
                            mediaController.play()
                        }
                        isPlayerLoading = isLoading
                        super.onIsLoadingChanged(isPlayerLoading)
                    }

//                    var previousPlaybackState = mediaController.playbackState
//
//                    override fun onPlaybackStateChanged(playbackState: Int) {
//                        if (previousPlaybackState == Player.STATE_BUFFERING && playbackState == Player.STATE_READY) {
//
//                        }
//                        previousPlaybackState = playbackState
//
//                        super.onPlaybackStateChanged(playbackState)
//                    }
                }
            )
        }

        Dialog(
            onDismissRequest = onDismissRequest,
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colors.surface)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val fooColor by animateColorAsState(
                        if (palette != null) {
                            if (MaterialTheme.colors.isLight)
                                Color(palette!!.getDarkVibrantColor(MaterialTheme.colors.onSurface.toArgb()))
                            else
                                Color(palette!!.getLightVibrantColor(MaterialTheme.colors.onSurface.toArgb()))
                        } else {
                            MaterialTheme.colors.onSurface.copy(0.5f)
                        }
                    )
                    Text(
                        text = "Прослушивание публикации",
                        color = fooColor,
                        fontWeight = FontWeight.W500,
                    )

                    Spacer(modifier = Modifier.height(16.dp))



                    AsyncImage(
                        imageLoader = ImageLoader.Builder(LocalContext.current)
                            .components {
                                this.add(Interceptor {
                                    val result = it.proceed(it.request)
                                    val bitmap = result.drawable?.toBitmap()
                                    bitmap?.let {
                                        Palette.from(it.copy(Bitmap.Config.ARGB_8888, true)).generate {
                                            palette = it
                                            Log.i("Palette", "Palette was generated!")
                                        }
                                    }
                                    result
                                })
                            }
                            .build(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colors.onSurface.copy(0.1f)),
                        model = mediaMetadata?.artworkUri,
                        contentDescription = null,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                onTitleClick()
                            }
                            .padding(4.dp)
                            .basicMarquee(),
                        text = article?.title ?: mediaMetadata?.title.toString(),
                        fontSize = 22.sp,
                        color = MaterialTheme.colors.onSurface,
                        fontWeight = FontWeight.W700,
                        maxLines = 1
                    )
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable(onClick = onAuthorClick)
                                .padding(4.dp),
                            text = article?.author?.alias?.let { "@$it" } ?: mediaMetadata?.author.toString(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.W500,
                            color = MaterialTheme.colors.onSurface.copy(0.5f)
                        )
                    }


                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {

                        PlayPauseButton(
                            mediaController
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colors.primary)
                                    .clickable(
                                        enabled = !isPlayerLoading,
                                        onClick = {
                                            if (article == null || article.id == articleMediaMetadata?.articleId) {
                                                onClick()
                                            } else {
                                                mediaController?.loadArticle(article!!.id)
                                            }
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {



                                if (
                                    (article != null && article.id == articleMediaMetadata?.articleId) ||
                                    article == null
                                ) {
                                    when {


                                        isEnabled && showPlay -> {
                                            Icon(
                                                modifier = Modifier.size(28.dp),
                                                imageVector = Icons.Filled.PlayArrow,
                                                contentDescription = null,
                                                tint = MaterialTheme.colors.onPrimary
                                            )
                                        }

                                        isEnabled && !showPlay -> {
                                            Icon(
                                                modifier = Modifier.size(28.dp),
                                                painter = painterResource(R.drawable.pause_icon),
                                                contentDescription = null,
                                                tint = MaterialTheme.colors.onPrimary
                                            )
                                        }

                                        else -> CircularProgressIndicator()
                                    }
                                } else {




                                    if (isPlayerLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(28.dp),
                                            color = MaterialTheme.colors.onPrimary
                                        )
                                    } else {
                                        Icon(
                                            modifier = Modifier.size(28.dp),
                                            imageVector = Icons.Filled.PlayArrow,
                                            contentDescription = null,
                                            tint = MaterialTheme.colors.onPrimary
                                        )
                                    }

                                }
                            }

                        }

                    }

                    if (article != null && articleMediaMetadata?.articleId != null && articleMediaMetadata.articleId != 0 && article.id != articleMediaMetadata?.articleId) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable(
                                    enabled = onCurrentPlayingClick != null
                                ) {
                                    onCurrentPlayingClick?.invoke()
                                }
                                .background(MaterialTheme.colors.primary)
                                .padding(vertical = 4.dp, horizontal = 12.dp)
                                .width(IntrinsicSize.Min),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                modifier = Modifier.weight(1f),
                                text = "Воспроизводится другая статья",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.W500,
                                color = MaterialTheme.colors.onPrimary,
                                textAlign = TextAlign.Center
                                )
                            Spacer(modifier = Modifier.width(4.dp))

                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "go to article that is playing now",
                                tint = MaterialTheme.colors.onPrimary
                            )
                        }

                    }
                }


            }
        }
    }
}