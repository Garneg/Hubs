package com.garnegsoft.hubs.ui.screens.article

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.garnegsoft.hubs.api.article.Article
import com.garnegsoft.hubs.ui.theme.HubsTheme


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Poll(
    poll: Article.Poll,
    onVote: (List<Int>) -> Unit,
    onPass: () -> Unit,
) {
    var selectedVariants by rememberSaveable { mutableStateOf(mutableListOf<Int>()) }
    val mostVotedVariantId = rememberSaveable {
        poll.variants.sortedByDescending { it.percent }.first().id
    }
    HubsTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = poll.title,
                fontWeight = FontWeight.W700,
                fontSize = 20.sp,
                color = MaterialTheme.colors.onBackground
            )
            if (!(poll.relatedData?.canVote ?: true)) {
                poll.variants.forEach {
                    PollItem(data = it, isMostVoted = it.id == mostVotedVariantId)
                }
            } else {
                if (poll.answersType == Article.Poll.PollType.Radio) {
                    poll.variants.forEach {
                        val interactionSource = remember { MutableInteractionSource() }
                        var selected by remember { mutableStateOf(false) }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    indication = null,
                                    interactionSource = interactionSource
                                ) {
                                    if (!selected) {
                                        selectedVariants.clear()
                                        selectedVariants.add(it.id)
                                        selected = true
                                    }
                                }
                        ) {

                            LaunchedEffect(key1 = selectedVariants.getOrNull(0), block = {
                                if (selectedVariants.getOrNull(0) != it.id)
                                    selected = false
                            })
                            RadioButton(selected = selected, onClick = {
                                if (!selected) {
                                    selectedVariants.clear()
                                    selectedVariants.add(it.id)
                                    selected = true
                                }

                            },
                            interactionSource = interactionSource)
                            Text(modifier = Modifier.padding(top = 12.dp), text = it.text)
                        }
                    }
                }
                else {
                    poll.variants.forEach {
                        var selected by remember { mutableStateOf(false) }
                        val interactionSource = remember { MutableInteractionSource() }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    indication = null,
                                    interactionSource = interactionSource
                                ) {
                                    val checked = !selected
                                    if (!checked) {
                                        selectedVariants.remove(it.id)
                                    } else {
                                        selectedVariants.add(it.id)
                                    }
                                    selected = checked
                                },

                            ) {
                            Checkbox(
                                checked = selected,
                                onCheckedChange = { checked ->
                                    if (!checked) {
                                        selectedVariants.remove(it.id)
                                    } else {
                                        selectedVariants.add(it.id)
                                    }
                                    selected = checked

                                },
                                interactionSource = interactionSource
                            )
                            Text(modifier = Modifier.padding(top = 12.dp), text = it.text)

                        }


                    }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        elevation = null,
                        shape = RoundedCornerShape(8.dp),
                        enabled = selectedVariants.size > 0,
                        contentPadding = PaddingValues(12.dp),
                        onClick = {
                            onVote(selectedVariants)
                        }
                    ) {
                        Text(text = "Голосовать", style = MaterialTheme.typography.body2)
                    }
                    OutlinedButton(
                        contentPadding = PaddingValues(12.dp),
                        onClick = onPass
                    ) {
                        Text(text = "Воздержаться", style = MaterialTheme.typography.body2)
                    }
                }
            }
            Text(
                text = "Проголосовали: ${poll.votesCount}. Воздержались ${poll.passCount}",
                color = MaterialTheme.colors.onBackground.copy(0.5f)
            )

        }
    }
}

@Composable
fun PollItem(
    data: Article.Poll.Variant,
    isMostVoted: Boolean
) {
    val colors = MaterialTheme.colors

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        val annotatedTitle = buildAnnotatedString {
            append("${data.text} ")
            withStyle(
                SpanStyle(
                    color = if (isMostVoted) colors.secondaryVariant else colors.secondaryVariant.copy(
                        0.7f
                    ), fontWeight = FontWeight.Bold
                )
            ) {
                append("(${data.percent}%)")
            }

        }
        Row {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = annotatedTitle,
                    color = MaterialTheme.colors.onBackground,
                    fontWeight = FontWeight.Medium,
                )
                if (data.selected) {
                    Icon(
                        modifier = Modifier.size(18.dp),
                        imageVector = Icons.Default.Done,
                        contentDescription = null,
                        tint = colors.secondaryVariant
                    )
                }
            }
            Text(
                text = data.votesCount.toString(),
                color = MaterialTheme.colors.onBackground.copy(0.6f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(2.dp))
            .height(12.dp), onDraw = {
            this.drawRect(color = colors.onBackground.copy(0.08f))
            val size =
                Size(width = this.size.width * data.percent / 100f, height = this.size.height)

            drawRect(
                color = if (isMostVoted) colors.secondaryVariant else colors.secondaryVariant.copy(
                    0.5f
                ), size = size
            )
        })
    }
}