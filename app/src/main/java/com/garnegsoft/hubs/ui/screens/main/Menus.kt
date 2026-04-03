package com.garnegsoft.hubs.ui.screens.main

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import coil.compose.AsyncImage
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.dataStore.HubsDataStore
import com.garnegsoft.hubs.ui.common.MenuItem

@Composable
fun AuthorizedMenu(
	userAlias: String,
	onProfileClick: () -> Unit,
	onArticlesClick: () -> Unit,
	onCommentsClick: () -> Unit,
	onBookmarksClick: () -> Unit,
	onSubscriptionsClick: () -> Unit,
	onSavedArticlesClick: () -> Unit,
	onHistoryClick: () -> Unit,
	onSettingsClick: () -> Unit,
	onAboutClick: () -> Unit,
) {
	var expanded by remember { mutableStateOf(false) }
	val avatarFilename by HubsDataStore.Auth.getValueFlow(
		LocalContext.current,
		HubsDataStore.Auth.AvatarFileName
	).collectAsState(initial = "")
	IconButton(onClick = { expanded = true }) {
		
		AsyncImage(
			modifier = Modifier
				.size(32.dp)
				.clip(RoundedCornerShape(8.dp))
				.background(if (MaterialTheme.colors.isLight) Color.Transparent else Color.White),
			contentScale = ContentScale.FillBounds,
			model = LocalContext.current.filesDir.toString() + "/$avatarFilename",
			contentDescription = ""
		)
		
		
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

	val scaleAnimatedValue by menuTransition.animateFloat(
		transitionSpec = { tween(durationMillis = 150, easing = EaseOutQuint) }
	) {
		if (it) 1f else 0.5f
	}

	val roundedCornersRadiusAnimatedValue by menuTransition.animateDp {
		if (it) 12.dp else 48.dp
	}
	
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
					.graphicsLayer {
						this.alpha = alpha

						translationY = -(size.height - (size.height * scaleAnimatedValue))/2
						translationX = (size.width - (size.width * scaleAnimatedValue))/2
						scaleX = scaleAnimatedValue
						scaleY = scaleAnimatedValue

					}
					.padding(4.dp)
			) {
				Surface(
					modifier = Modifier
						.graphicsLayer {
							shape = RoundedCornerShape(roundedCornersRadiusAnimatedValue)
							clip = true
							shadowElevation = 4.dp.roundToPx().toFloat()
						}
//						.shadow(4.dp, RoundedCornerShape(12.dp))
						.clip(RoundedCornerShape(roundedCornersRadiusAnimatedValue))
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
								
								AsyncImage(
									modifier = Modifier
										.size(32.dp)
										.clip(RoundedCornerShape(8.dp))
										.background(Color.White),
									contentScale = ContentScale.FillBounds,
									model = LocalContext.current.filesDir.toString() + "/$avatarFilename",
									contentDescription = ""
								)
								
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
									contentDescription = ""
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
								)
							}, onClick = {
								onBookmarksClick()
								expanded = false
							}
						)

						MenuItem(
							modifier = Modifier.graphicsLayer {
								this.translationY = -itemsOffset + itemsOffset * itemsAnimation
								this.alpha = itemsAnimation + 0.35f
							},
							title = "Подписки", icon = {
								Icon(
									painter = painterResource(id = R.drawable.group),
									contentDescription = "",
								)
							}, onClick = {
								onSubscriptionsClick()
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
			)
		}, onClick = {
			onLoginClick()
			expanded = false
		})
		
		MenuItem(title = "Скачанные", icon = {
			Icon(
				painter = painterResource(id = R.drawable.download),
				contentDescription = "",
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
			)
		}, onClick = {
			onSettingsClick()
			expanded = false
		})
		
		Divider(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
		
		MenuItem(
			title = "О приложении",
			icon = {
				Icon(
					imageVector = Icons.Outlined.Info,
					contentDescription = "",
					modifier = Modifier.size(24.dp),
				)
			},
			onClick = {
				onAboutClick()
				expanded = false
			})
	}
}

