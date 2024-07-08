package com.garnegsoft.hubs.ui.screens.user


import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.*
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.garnegsoft.hubs.data.comment.list.CommentSnippet
import com.garnegsoft.hubs.data.rememberCollapsingContentState
import com.garnegsoft.hubs.data.utils.formatLongNumbers
import com.garnegsoft.hubs.ui.common.*
import com.garnegsoft.hubs.ui.common.feedCards.comment.CommentCard
import com.garnegsoft.hubs.ui.common.snippetsPages.ArticlesListPageWithFilter
import com.garnegsoft.hubs.ui.common.snippetsPages.CommentsListPage
import com.garnegsoft.hubs.ui.common.snippetsPages.CommonPageWithFilter
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
							viewModel.user.value?.let { user ->
								val sendIntent = Intent(Intent.ACTION_SEND)
								sendIntent.putExtra(
									Intent.EXTRA_TEXT,
									if (user.fullname != null)
										"${user.fullname} (@${user.alias}) — https://habr.com/ru/users/${user.alias}/"
									else
										"${user.alias} — https://habr.com/ru/users/${user.alias}/"
								)
								sendIntent.setType("text/plain")
								val shareIntent = Intent.createChooser(sendIntent, null)
								context.startActivity(shareIntent)
							}
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
		
		val profilePageScrollState = rememberScrollState()
		
		val articlesLazyListState = rememberLazyListState()
		val articlesFilterContentState = rememberCollapsingContentState()
		
		val commentsLazyListState = rememberLazyListState()
		
		val bookmarksLazyListState = rememberLazyListState()
		val bookmarksFilterContentState = rememberCollapsingContentState()
		
		val followersLazyListState = rememberLazyListState()
		
		val subscriptionsLazyListState = rememberLazyListState()
		
		
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
									profilePageScrollState,
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
							lazyListState = articlesLazyListState,
							collapsingContentState = articlesFilterContentState,
							onArticleSnippetClick = onArticleClicked,
							onArticleAuthorClick = onUserClicked,
							onArticleCommentsClick = onCommentsClicked,
							ignoreBlackList = true
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
							lazyListState = commentsLazyListState,
							onArticleClick = onArticleClicked,
							onCommentClick = onCommentClicked,
							onUserClick = onUserClicked
						)
					}
				}
				if (usr.bookmarksCount > 0 || initialPage == UserScreenPages.Bookmarks) {
					map += UserScreenPages.Bookmarks to {
						val bookmarksFilter by viewModel.bookmarksFilter.observeAsState(initial = null)
						bookmarksFilter?.let {
							if (it.bookmarks != UserBookmarksFilter.Bookmarks.Comments) {
								ArticlesListPageWithFilter(
									listModel = viewModel.bookmarksModel,
									lazyListState = bookmarksLazyListState,
									collapsingContentState = bookmarksFilterContentState,
									onArticleSnippetClick = onArticleClicked,
									onArticleAuthorClick = onUserClicked,
									onArticleCommentsClick = onCommentsClicked
								) { defaultValues, onDismiss, onDone ->
									UserBookmarksFilter(
										defaultValues = defaultValues,
										onDismiss = onDismiss,
										onDone = {
											viewModel.bookmarksFilter.value = it
											if (it.bookmarks != UserBookmarksFilter.Bookmarks.Comments)
												onDone(it)
										}
									)
								}
							} else {
								CommonPageWithFilter<CommentSnippet, UserBookmarksFilter>(
									listModel = viewModel.commentsBookmarksModel,
									filterDialog = { defaultValues, onDismiss, onDone ->
										UserBookmarksFilter(
											defaultValues = defaultValues,
											onDismiss = onDismiss,
											onDone = {
												viewModel.bookmarksFilter.value = it
												onDone(it)
											}
										)
									}
								) {
									CommentCard(
										comment = it,
										onCommentClick = {
											onCommentClicked(
												it.parentPost.id,
												it.id
											)
										},
										onAuthorClick = { onUserClicked(it.author.alias) },
										onParentPostClick = { onArticleClicked(it.parentPost.id) })
								}
							}
						}
						
						Unit
						
						
					}
				}
				if (usr.followersCount > 0 || initialPage == UserScreenPages.Followers) {
					map += UserScreenPages.Followers to {
						UsersListPage(
							listModel = viewModel.followersModel,
							lazyListState = followersLazyListState,
							onUserClick = onUserClicked
						)
					}
				}
				if (usr.subscriptionsCount > 0 || initialPage == UserScreenPages.Follows) {
					map += UserScreenPages.Follows to {
						UsersListPage(
							listModel = viewModel.subscriptionsModel,
							lazyListState = subscriptionsLazyListState,
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
							if (viewModel.user.value!!.bookmarksCount > 0) " " + formatLongNumbers(
								viewModel.user.value!!.bookmarksCount
							) else ""
						}"
						
						UserScreenPages.Followers -> "Подписчики${
							if (viewModel.user.value!!.followersCount > 0) " " + formatLongNumbers(
								viewModel.user.value!!.followersCount
							) else ""
						}"
						
						UserScreenPages.Follows -> "Подписки${
							if (viewModel.user.value!!.subscriptionsCount > 0) " " + formatLongNumbers(
								viewModel.user.value!!.subscriptionsCount
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
					HabrScrollableTabRow(pagerState = pagerState, tabs = tabs) { index, title ->
						when {
							title.startsWith("Профиль") -> {
								ScrollUpMethods.scrollNormalList(profilePageScrollState)
							}
							
							title.startsWith("Публикации") -> {
								articlesFilterContentState.show()
								ScrollUpMethods.scrollLazyList(articlesLazyListState)
							}
							
							title.startsWith("Комментарии") -> {
								ScrollUpMethods.scrollLazyList(commentsLazyListState)
							}
							
							title.startsWith("Закладки") -> {
								bookmarksFilterContentState.show()
								ScrollUpMethods.scrollLazyList(bookmarksLazyListState)
							}
							
							title.startsWith("Подписчики") -> {
								ScrollUpMethods.scrollLazyList(followersLazyListState)
							}
							
							title.startsWith("Подписки") -> {
								ScrollUpMethods.scrollLazyList(subscriptionsLazyListState)
							}
							
						}
					}
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
			HubsCircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
		}
	}
	
}
