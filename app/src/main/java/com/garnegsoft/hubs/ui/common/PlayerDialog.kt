package com.garnegsoft.hubs.ui.common

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.Color
import android.graphics.RenderNode
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.sharp.Menu
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.colorspace.ColorSpace
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.drawable.toBitmap
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.ui.compose.buttons.PlayPauseButton
import androidx.media3.ui.compose.state.PlayPauseButtonState
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.intercept.Interceptor
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.article.Article
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import kotlin.coroutines.CoroutineContext


@OptIn(UnstableApi::class)
@Composable
fun PlayerDialog(
    show: Boolean,
    onDismissRequest: () -> Unit,
    mediaController: MediaController?,
    onTitleClick: () -> Unit,
    onAuthorClick: () -> Unit,
    article: Article?
) {
    if (show) {
        var palette by remember { mutableStateOf<Palette?>(null) }

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
                        model = mediaController?.mediaMetadata?.artworkUri,
                        contentDescription = null,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onTitleClick()
                            }
                            .padding(4.dp)
                            .basicMarquee(),
                        text = article?.title ?: mediaController?.mediaMetadata?.title.toString(),
                        fontSize = 22.sp,
                        color = MaterialTheme.colors.onSurface,
                        fontWeight = FontWeight.W700,
                        maxLines = 1
                    )

                    Text(
                        modifier = Modifier
                            .padding(4.dp)
                            .clickable(onClick = onAuthorClick)
                            .fillMaxWidth(),
                        text = article?.author?.toString() ?: mediaController?.mediaMetadata?.author.toString(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W500,
                        color = MaterialTheme.colors.onSurface.copy(0.5f)
                    )

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
                                    .clickable(onClick = { onClick() }),
                                contentAlignment = Alignment.Center
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
                            }

                        }

                    }
                }


            }
        }
    }
}