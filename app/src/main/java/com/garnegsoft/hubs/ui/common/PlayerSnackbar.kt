package com.garnegsoft.hubs.ui.common

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.ui.compose.state.PlayPauseButtonState
import coil.compose.AsyncImage
import com.garnegsoft.hubs.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow


@OptIn(UnstableApi::class)
@Composable
fun PlayerSnackbar(
    onClick: () -> Unit,
    mediaController: MediaController,
    playPauseButtonState: PlayPauseButtonState,
    modifier: Modifier = Modifier,
) {
    mediaController.currentPosition / mediaController.duration

    Box(
        modifier = modifier
            .padding(12.dp)
            .fillMaxWidth()
            .widthIn(max = 550.dp)
            .shadow(4.dp, shape = RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .background(MaterialTheme.colors.surface.run {
                if (MaterialTheme.colors.isLight)
                    copy(1f, red * 0.95f, green * 0.95f, blue * 0.95f)
                else
                    copy(1f, red * 1.75f, green * 1.83f, blue * 1.9f)
            })
            .padding(8.dp),
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
        ) {
            AsyncImage(
                modifier = Modifier
                    .size(48.dp)
                    .border(
                        width = 0.5.dp,
                        color = MaterialTheme.colors.onSurface.copy(0.1f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .clip(RoundedCornerShape(4.dp)),
                model = mediaController?.mediaMetadata?.artworkUri,
                contentDescription = "Picture to draw attention to an article",
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.article)
            )

            val progressState by remember { flow { while (true) { emit(mediaController.currentPosition.toFloat() / mediaController.duration.toFloat()); delay(1000)
            } } }.collectAsState(0f)

            Spacer(modifier = Modifier.width(8.dp))
            val progressIndicatorColor = MaterialTheme.colors.secondary.copy(0.7f)
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight()
                    .padding(top = 2.dp)
                    .drawBehind {
                        drawRoundRect(
                            color = progressIndicatorColor.copy(0.1f),
                            topLeft = Offset(x = 0f, y = size.height - (4 * density)),
                            size = size.copy(height = 3 * density),
                            cornerRadius = CornerRadius(3 * density, y = 3 * density)
                        )
                        drawRoundRect(
                            color = progressIndicatorColor,
                            topLeft = Offset(x = 0f, y = size.height - (4 * density)),
                            size = size.copy((size.width * progressState).coerceAtLeast(3f * 2f * density), height = 3 * density),
                            cornerRadius = CornerRadius(3 * density, y = 3 * density)
                        )
                    },
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    modifier = Modifier.basicMarquee(3),
                    text = mediaController?.mediaMetadata?.title?.toString() ?: "Проигрывается статья",
                    fontWeight = FontWeight.W700,
                    maxLines = 1,
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = mediaController?.mediaMetadata?.artist?.toString() ?: "Неизвестный автор",
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.W500,
                    color = MaterialTheme.colors.onSurface.copy(0.5f)
                )

            }


            Spacer(modifier = Modifier.width(8.dp))

            IconButton(

                onClick = {
                    playPauseButtonState.onClick()
                }
            ) {
                if (playPauseButtonState.showPlay) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "play",
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.pause_icon),
                        contentDescription = "pause"
                    )
                }
            }
        }
    }
}