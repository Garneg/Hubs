package com.garnegsoft.hubs.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.media3.session.MediaController
import androidx.media3.ui.compose.buttons.PlayPauseButton
import androidx.media3.ui.compose.state.PlayPauseButtonState
import coil.compose.AsyncImage
import com.garnegsoft.hubs.api.article.Article


@Composable
fun PlayerDialog(
    show: Boolean,
    onDismissRequest: () -> Unit,
    mediaController: MediaController,
    article: Article?
) {
    if (show) {
        Dialog(
            onDismissRequest = onDismissRequest,
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colors.surface)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Прослушивание публикации",
                        color = MaterialTheme.colors.onSurface.copy(0.5f),
                        fontWeight = FontWeight.W500,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AsyncImage(
                        modifier = Modifier.fillMaxWidth(),
                        model = mediaController.mediaMetadata.artworkUri,
                        contentDescription = null,
                        )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .basicMarquee(),
                        text = article?.title ?: mediaController.mediaMetadata.title.toString(),
                        fontSize = 20.sp,
                        color = MaterialTheme.colors.onSurface,
                        fontWeight = FontWeight.W700,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {

                        PlayPauseButton(
                            mediaController
                        ) {
                            IconButton(
                                onClick = { onClick() }
                            ) {
                                when {
                                    isEnabled && showPlay -> Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = null)
                                    isEnabled && !showPlay-> Icon(imageVector = Icons.Sharp.Menu, contentDescription = null)
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