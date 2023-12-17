package com.garnegsoft.hubs.ui.screens.main

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import coil.compose.AsyncImage
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.utils.placeholderColorLegacy

@Composable
fun AuthorizedMenu(
	userAlias: String,
	avatarUrl: String?,
	onProfileClick: () -> Unit,
	onArticlesClick: () -> Unit,
	onCommentsClick: () -> Unit,
	onBookmarksClick: () -> Unit,
	onSavedArticlesClick: () -> Unit,
	onHistoryClick: () -> Unit,
	onSettingsClick: () -> Unit,
	onAboutClick: () -> Unit,
) {
	var expanded by remember { mutableStateOf(false) }
	IconButton(onClick = { expanded = true }) {
		if (avatarUrl != null) {
			AsyncImage(
				modifier = Modifier
					.size(32.dp)
					.clip(RoundedCornerShape(8.dp))
					.background(if (MaterialTheme.colors.isLight) Color.Transparent else Color.White),
				contentScale = ContentScale.FillBounds,
				model = avatarUrl, contentDescription = ""
			)
		} else {
			Icon(
				modifier = Modifier
					.size(32.dp)
					.clip(RoundedCornerShape(8.dp))
					.border(
						width = 2.dp, color = placeholderColorLegacy(userAlias),
						shape = RoundedCornerShape(8.dp)
					)
					.padding(1.dp)
					.background(Color.White)
					.padding(1.5.dp),
				painter = painterResource(id = R.drawable.user_avatar_placeholder),
				contentDescription = "",
				tint = placeholderColorLegacy(userAlias)
			)
		}
		
	}
	val menuTransition = updateTransition(targetState = expanded)
	
	val alpha by menuTransition.animateFloat(
		transitionSpec = {
			if (this.targetState)
				tween(150)
			else
				tween(150)
			
		}
	) {
		if (it) 1f else 0.0f
	}
	
	val itemsAnimation by menuTransition.animateFloat(
		transitionSpec = {
			if (this.targetState)
				tween(150, 50)
			else
				tween(100)
			
		}
	) {
		if (it) 1f else 0.0f
	}
	
	val itemsOffset = 20
	
	if (menuTransition.targetState || expanded || menuTransition.currentState) {
		Popup(
			popupPositionProvider = object : PopupPositionProvider {
				override fun calculatePosition(
					anchorBounds: IntRect,
					windowSize: IntSize,
					layoutDirection: LayoutDirection,
					popupContentSize: IntSize
				): IntOffset {
					val shit = anchorBounds.right
					return IntOffset(shit - popupContentSize.width, anchorBounds.bottom)
				}
				
			},
			properties = PopupProperties(true),
			onDismissRequest = { expanded = false }
		) {
			
			Box(
				modifier = Modifier
					.alpha(alpha)
					.padding(4.dp)
			) {
				Surface(
					modifier = Modifier
						.shadow(4.dp, RoundedCornerShape(8.dp))
						.clip(RoundedCornerShape(8.dp))
						.background(MaterialTheme.colors.surface),
					elevation = 4.dp
				) {
					Column(
						Modifier
							.width(intrinsicSize = IntrinsicSize.Max)
							.widthIn(min = 150.dp)
							.verticalScroll(rememberScrollState())
					) {
						MenuItem(
							modifier = Modifier.graphicsLayer {
								this.translationY = -itemsOffset + itemsOffset * itemsAnimation
								this.alpha = itemsAnimation + 0.6f
							},
							title = userAlias, icon = {
								if (avatarUrl != null) {
									AsyncImage(
										modifier = Modifier
											.size(32.dp)
											.clip(RoundedCornerShape(8.dp))
											.background(Color.White),
										contentScale = ContentScale.FillBounds,
										model = avatarUrl, contentDescription = ""
									)
								} else {
									Icon(
										modifier = Modifier
											.size(32.dp)
											.border(
												width = 2.dp,
												color = placeholderColorLegacy(userAlias),
												shape = RoundedCornerShape(8.dp)
											)
											.background(
												Color.White,
												shape = RoundedCornerShape(8.dp)
											)
											.padding(2.5.dp),
										painter = painterResource(id = R.drawable.user_avatar_placeholder),
										contentDescription = "",
										tint = placeholderColorLegacy(userAlias)
									)
								}
							}, onClick = {
								onProfileClick()
								expanded = false
							}
						)
						Divider(
							modifier = Modifier
								.padding(
									horizontal = 12.dp,
									vertical = 4.dp
								)
								.graphicsLayer {
									this.translationY =
										-itemsOffset + itemsOffset * itemsAnimation
									this.alpha = itemsAnimation + 0.6f
								}
						)
						
						MenuItem(
							modifier = Modifier.graphicsLayer {
								this.translationY = -itemsOffset + itemsOffset * itemsAnimation
								this.alpha = itemsAnimation + 0.55f
							},
							title = "Статьи", icon = {
								Icon(
									painter = painterResource(id = R.drawable.article),
									contentDescription = "",
									tint = MaterialTheme.colors.onBackground
								)
							}, onClick = {
								onArticlesClick()
								expanded = false
							}
						)
						
						MenuItem(
							modifier = Modifier.graphicsLayer {
								this.translationY = -itemsOffset + itemsOffset * itemsAnimation
								this.alpha = itemsAnimation + 0.5f
							},
							title = "Комментарии", icon = {
								Icon(
									painter = painterResource(id = R.drawable.comments_icon),
									contentDescription = "",
									tint = MaterialTheme.colors.onBackground
								)
							}, onClick = {
								onCommentsClick()
								expanded = false
							}
						)
						
						MenuItem(
							modifier = Modifier.graphicsLayer {
								this.translationY = -itemsOffset + itemsOffset * itemsAnimation
								this.alpha = itemsAnimation + 0.4f
							},
							title = "Закладки", icon = {
								Icon(
									painter = painterResource(id = R.drawable.bookmark),
									contentDescription = "",
									tint = MaterialTheme.colors.onBackground
								)
							}, onClick = {
								onBookmarksClick()
								expanded = false
							}
						)
						
						MenuItem(
							modifier = Modifier.graphicsLayer {
								this.translationY = -itemsOffset + itemsOffset * itemsAnimation
								this.alpha = itemsAnimation + 0.3f
							},
							title = "Скачанные", icon = {
								Icon(
									painter = painterResource(id = R.drawable.download),
									contentDescription = "",
									tint = MaterialTheme.colors.onBackground
								)
							}, onClick = {
								onSavedArticlesClick()
								expanded = false
							}
						)
						
						MenuItem(
							modifier = Modifier.graphicsLayer {
								this.translationY = -itemsOffset + itemsOffset * itemsAnimation
								this.alpha = itemsAnimation + 0.2f
							},
							title = "История",
							icon = {
								Icon(
									painter = painterResource(id = R.drawable.history),
									contentDescription = null,
									tint = MaterialTheme.colors.onBackground
								)
							},
							onClick = {
								onHistoryClick()
								expanded = false
							}
						)
						
						MenuItem(
							modifier = Modifier.graphicsLayer {
								this.translationY = -itemsOffset + itemsOffset * itemsAnimation
								this.alpha = itemsAnimation + 0.1f
							},
							title = "Настройки", icon = {
								Icon(
									imageVector = Icons.Outlined.Settings,
									contentDescription = "",
									tint = MaterialTheme.colors.onBackground
								)
							}, onClick = {
								onSettingsClick()
								expanded = false
							}
						)
						
						Divider(
							modifier = Modifier
								.padding(
									horizontal = 12.dp,
									vertical = 4.dp
								)
								.graphicsLayer {
									this.translationY =
										-itemsOffset + itemsOffset * itemsAnimation
									this.alpha = itemsAnimation + 0.05f
								}
						)
						
						MenuItem(
							modifier = Modifier.graphicsLayer {
								this.translationY = -itemsOffset + itemsOffset * itemsAnimation
								this.alpha = itemsAnimation + 0.0f
							},
							title = "О приложении", icon = {
								Icon(
									imageVector = Icons.Outlined.Info,
									contentDescription = "",
									tint = MaterialTheme.colors.onBackground
								)
							}, onClick = {
								onAboutClick()
								expanded = false
							}
						)
					}
				}
			}
		}
	}
}


@Composable
fun UnauthorizedMenu(
	onLoginClick: () -> Unit,
	onAboutClick: () -> Unit,
	onSettingsClick: () -> Unit,
	onHistoryClick: () -> Unit,
	onSavedArticlesClick: () -> Unit
) {
	var expanded by remember { mutableStateOf(false) }
	IconButton(onClick = { expanded = true }) {
		Icon(imageVector = Icons.Outlined.MoreVert, contentDescription = "menu")
	}
	
	DropdownMenu(
		expanded = expanded,
		onDismissRequest = { expanded = false },
		modifier = Modifier.width(intrinsicSize = IntrinsicSize.Max)
	) {
		MenuItem(title = "Войти", icon = {
			Icon(
				imageVector = Icons.Outlined.ExitToApp,
				contentDescription = "",
				tint = MaterialTheme.colors.onBackground
			)
		}, onClick = {
			onLoginClick()
			expanded = false
		})
		
		MenuItem(title = "Скачанные", icon = {
			Icon(
				painter = painterResource(id = R.drawable.download),
				contentDescription = "",
				tint = MaterialTheme.colors.onBackground
			)
		}, onClick = {
			onSavedArticlesClick()
			expanded = false
		})
		
		MenuItem(
			title = "История",
			icon = {
				Icon(
					painter = painterResource(id = R.drawable.history),
					contentDescription = null,
					tint = MaterialTheme.colors.onBackground
				)
			},
			onClick = {
				onHistoryClick()
				expanded = false
			}
		)
		
		MenuItem(title = "Настройки", icon = {
			Icon(
				imageVector = Icons.Outlined.Settings,
				contentDescription = "",
				tint = MaterialTheme.colors.onBackground
			)
		}, onClick = {
			onSettingsClick()
			expanded = false
		})
		
		Divider(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
		
		MenuItem(title = "О приложении", icon = {
			Icon(
				imageVector = Icons.Outlined.Info,
				contentDescription = "",
				modifier = Modifier.size(24.dp),
				tint = MaterialTheme.colors.onBackground
			)
		}, onClick = {
			onAboutClick()
			expanded = false
		})
	}
}

@Composable
fun MenuItem(
	title: String,
	modifier: Modifier = Modifier,
	icon: @Composable () -> Unit,
	onClick: () -> Unit
) {
	Row(
		modifier = modifier
			.clickable(onClick = onClick)
			.padding(14.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		icon()
		Spacer(modifier = Modifier.width(14.dp))
		Text(
			title,
			color = MaterialTheme.colors.onBackground
		)
		Spacer(modifier = Modifier.width(14.dp))
		Spacer(modifier = Modifier.weight(1f))
	}
}