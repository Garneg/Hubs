package com.garnegsoft.hubs.ui.screens.user


import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.Text
import androidx.compose.material.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.*
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.garnegsoft.hubs.api.utils.formatLongNumbers
import com.garnegsoft.hubs.ui.common.*
import com.garnegsoft.hubs.ui.common.snippetsPages.ArticlesListPage
import com.garnegsoft.hubs.ui.common.snippetsPages.ArticlesListPageWithFilter
import com.garnegsoft.hubs.ui.common.snippetsPages.CommentsListPage
import com.garnegsoft.hubs.ui.common.snippetsPages.UsersListPage


enum class UserScreenPages {
	Profile,
	Articles,
	Comments,
	Bookmarks,
	Followers,
	Follows
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserScreen(
	alias: String,
	onBack: () -> Unit,
	onArticleClicked: (articleId: Int) -> Unit,
	onCommentClicked: (parentArticleId: Int, commentId: Int) -> Unit,
	onUserClicked: (alias: String) -> Unit,
	onCommentsClicked: (postId: Int) -> Unit,
	onHubClicked: (alias: String) -> Unit,
	onCompanyClick: (alias: String) -> Unit,
	initialPage: UserScreenPages = UserScreenPages.Profile,
	isAppUser: Boolean = false,
	onLogout: (() -> Unit)? = null,
	viewModelStoreOwner: ViewModelStoreOwner,
) {
	val viewModel = viewModel { UserScreenViewModel(alias) }
	
	Scaffold(
		topBar = {
			TopAppBar(
				elevation = 0.dp,
				title = { Text("Пользователь") },
				navigationIcon = {
					IconButton(onClick = onBack) {
						Icon(imageVector = Icons.Default.ArrowBack, "")
					}
				},
				actions = {
					val context = LocalContext.current
					
					IconButton(
						onClick = {
							val sendIntent = Intent(Intent.ACTION_SEND)
							sendIntent.putExtra(
								Intent.EXTRA_TEXT,
								if (viewModel.user.value?.fullname != null)
									"${viewModel.user.value!!.fullname} — https://habr.com/ru/users/${viewModel.user.value!!.alias}/"
								else
									"${viewModel.user.value!!.alias} — https://habr.com/ru/users/${viewModel.user.value!!.alias}/"
							)
							sendIntent.setType("text/plain")
							val shareIntent = Intent.createChooser(sendIntent, null)
							context.startActivity(shareIntent)
						}) {
						Icon(imageVector = Icons.Outlined.Share, contentDescription = "")
					}
				}
			)
		}
	) {
		LaunchedEffect(key1 = Unit) {
			if (!viewModel.user.isInitialized) {
				viewModel.loadUserProfile()
			}
		}
		viewModel.user.observeAsState().value?.let { usr ->
			var pagesMap = remember {
				var map = mapOf<UserScreenPages, @Composable () -> Unit>(
					UserScreenPages.Profile to {
						val user by viewModel.user.observeAsState()
						if (user != null) {
							
							val note by viewModel.note.observeAsState()
							val hubs by viewModel.subscribedHubs.observeAsState()
							val whoIs by viewModel.whoIs.observeAsState()
							
							if (hubs != null)
								UserProfile(
									isAppUser,
									onLogout,
									onHubClicked,
									onCompanyClick,
									viewModel,
								)
							else
								LaunchedEffect(key1 = Unit, block = {
									viewModel.loadNote()
									viewModel.loadSubscribedHubs()
									viewModel.loadWhoIs()
								})
						}
					})
				if (usr.articlesCount > 0 || initialPage == UserScreenPages.Articles) {
					map += UserScreenPages.Articles to {
						ArticlesListPageWithFilter(
							listModel = viewModel.articlesModel,
							onArticleSnippetClick = onArticleClicked,
							onArticleAuthorClick = onUserClicked,
							onArticleCommentsClick = onCommentsClicked
						) { defaultValues, onDismiss, onDone ->
							UserArticlesFilter(
								defaultValues = defaultValues,
								onDismiss = onDismiss,
								onDone = onDone
							)
						}
					}
				}
				if (usr.commentsCount > 0 || initialPage == UserScreenPages.Comments) {
					map += UserScreenPages.Comments to {
						CommentsListPage(
							listModel = viewModel.commentsModel,
							onArticleClick = onArticleClicked,
							onCommentClick = onCommentClicked,
							onUserClick = onUserClicked
						)
					}
				}
				if (usr.favoritesCount > 0 || initialPage == UserScreenPages.Bookmarks) {
					map += UserScreenPages.Bookmarks to {
						ArticlesListPage(
							listModel = viewModel.bookmarksModel,
							onArticleSnippetClick = onArticleClicked,
							onArticleAuthorClick = onUserClicked,
							onArticleCommentsClick = onCommentsClicked
						)
					}
				}
				if (usr.followersCount > 0 || initialPage == UserScreenPages.Followers) {
					map += UserScreenPages.Followers to {
						UsersListPage(
							listModel = viewModel.followersModel,
							onUserClick = onUserClicked
						)
					}
				}
				if (usr.followsCount > 0 || initialPage == UserScreenPages.Follows) {
					map += UserScreenPages.Follows to {
						UsersListPage(
							listModel = viewModel.followsModel,
							onUserClick = onUserClicked
						)
					}
				}
				map
			}
			val tabs = remember {
				pagesMap.keys.map {
					when (it) {
						UserScreenPages.Profile -> "Профиль"
						UserScreenPages.Articles -> "Публикации${
							if (viewModel.user.value!!.articlesCount > 0) " " + formatLongNumbers(
								viewModel.user.value!!.articlesCount
							) else ""
						}"
						
						UserScreenPages.Comments -> "Комментарии${
							if (viewModel.user.value!!.commentsCount > 0) " " + formatLongNumbers(
								viewModel.user.value!!.commentsCount
							) else ""
						}"
						
						UserScreenPages.Bookmarks -> "Закладки${
							if (viewModel.user.value!!.favoritesCount > 0) " " + formatLongNumbers(
								viewModel.user.value!!.favoritesCount
							) else ""
						}"
						
						UserScreenPages.Followers -> "Подписчики${
							if (viewModel.user.value!!.followersCount > 0) " " + formatLongNumbers(
								viewModel.user.value!!.followersCount
							) else ""
						}"
						
						UserScreenPages.Follows -> "Подписки${
							if (viewModel.user.value!!.followsCount > 0) " " + formatLongNumbers(
								viewModel.user.value!!.followsCount
							) else ""
						}"
					}
				}
			}
			val pagerState = rememberPagerState(initialPage = remember {
				pagesMap.keys.indexOf(initialPage)
			}) { pagesMap.size }
			
			Column(modifier = Modifier.padding(it)) {
				if (pagesMap.size > 1) {
					HabrScrollableTabRow(pagerState = pagerState, tabs = tabs)
				}
				HorizontalPager(
					state = pagerState
				) { pageIndex ->
					pagesMap.values.elementAt(pageIndex).invoke()
				}
			}
		} ?: Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(it)
		) {
			CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
		}
	}
	
}
