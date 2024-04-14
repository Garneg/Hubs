package com.garnegsoft.hubs.ui.common.feedCards.article

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.garnegsoft.hubs.data.dataStore.HubsDataStore
import com.garnegsoft.hubs.ui.theme.SecondaryColor

/**
 * Style of the [ArticleCard]
 */
@Immutable
data class ArticleCardStyle(
	val innerPadding: Dp = 20.dp,
	val innerElementsShape: Shape = RoundedCornerShape(4.dp),
	val cardShape: Shape = RoundedCornerShape(0.dp),
	
	val showImage: Boolean = true,
	
	val showTextSnippet: Boolean = true,
	
	val showHubsList: Boolean = true,
	
	val commentsButtonEnabled: Boolean = true,
	
	/**
	 * Sets whether corresponding button will be clickable or not.
	 * Note that, even if this parameter set to true,
	 * button can be disabled if user haven't logged in yet.
	 *
	 * All you can do with this parameter is to restrict use of bookmarks button
	 */
	val bookmarksButtonAllowedBeEnabled: Boolean = true,
	
	val backgroundColor: Color = Color.White,
	
	val textColor: Color = Color.Black,
	
	val authorAvatarSize: Dp = 24.dp,
	
	val snippetMaxLines: Int = 4,
	
	val rippleColor: Color = textColor,
	
	val imageLoadingIndicatorColor: Color = SecondaryColor,
	
	val titleTextStyle: TextStyle = TextStyle(
		color = textColor,
		fontSize = 20.sp,
		fontWeight = FontWeight.W700,
	),
	
	val snippetTextStyle: TextStyle = TextStyle(
		color = textColor.copy(alpha = 0.75f),
		fontSize = 16.sp,
		fontWeight = FontWeight.W400,
	),
	
	val authorTextStyle: TextStyle = TextStyle(
		color = textColor,
		fontSize = 14.sp,
		fontWeight = FontWeight.W600
	),
	
	val publishedTimeTextStyle: TextStyle = TextStyle(
		color = textColor.copy(alpha = 0.5f),
		fontSize = 12.sp,
		fontWeight = FontWeight.W400
	),
	
	/**
	 * Text style of statistics row,
	 * note that statistics color can be overridden for score indicator
	 */
	val statisticsColor: Color = textColor.copy(alpha = 0.75f),
	
	val statisticsTextStyle: TextStyle = TextStyle(
		color = statisticsColor,
		fontSize = 14.sp,
		fontWeight = FontWeight.W400
	),
	
	val hubsTextStyle: TextStyle = TextStyle(
		color = textColor.copy(alpha = 0.5f),
		fontSize = 14.sp,
		fontWeight = FontWeight.W500
	)

) {
	companion object {
		
		@Composable
		fun defaultArticleCardStyle(): ArticleCardStyle? {
			val showImage by HubsDataStore.Settings.getValueFlow(
				LocalContext.current,
				HubsDataStore.Settings.ArticleCard.ShowImage
			).collectAsState(initial = null)
			
			val showTextSnippet by HubsDataStore.Settings.getValueFlow(
				LocalContext.current,
				HubsDataStore.Settings.ArticleCard.ShowTextSnippet
			).collectAsState(initial = null)
			
			val textSnippetFontSize by HubsDataStore.Settings.getValueFlow(
				LocalContext.current,
				HubsDataStore.Settings.ArticleCard.TextSnippetFontSize
			).collectAsState(initial = null)
			
			val textSnippetMaxLines by HubsDataStore.Settings.getValueFlow(
				LocalContext.current,
				HubsDataStore.Settings.ArticleCard.TextSnippetMaxLines
			).collectAsState(initial = null)
			
			val titleFontSize by HubsDataStore.Settings.getValueFlow(
				LocalContext.current,
				HubsDataStore.Settings.ArticleCard.TitleFontSize
			).collectAsState(initial = null)
			
			if (showImage == null || showTextSnippet == null
				|| textSnippetFontSize == null || textSnippetMaxLines == null
				|| titleFontSize == null)
				return null
			
			val colors = MaterialTheme.colors
			val defaultCardStyle = remember {
				ArticleCardStyle(textColor = colors.onSurface,
					statisticsColor = colors.onSurface.copy(
					alpha = if (colors.isLight) {
						0.6f
					} else {
						0.5f
					}
					
					))
			}
			
			return defaultCardStyle.copy(
				backgroundColor = MaterialTheme.colors.surface,
				textColor = MaterialTheme.colors.onSurface,
				statisticsColor = MaterialTheme.colors.onSurface
					.copy(
						alpha = if (MaterialTheme.colors.isLight) {
							0.6f
						} else {
							0.5f
						}
					
					),
				snippetTextStyle = defaultCardStyle.snippetTextStyle.copy(
					fontSize = textSnippetFontSize!!.sp
				),
				titleTextStyle = defaultCardStyle.titleTextStyle.copy(fontSize = titleFontSize!!.sp),
				showImage = showImage!!,
				showTextSnippet = showTextSnippet!!,
				snippetMaxLines = textSnippetMaxLines!!
			)
		}
	}
}


