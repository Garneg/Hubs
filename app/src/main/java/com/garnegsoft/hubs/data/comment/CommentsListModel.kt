package com.garnegsoft.hubs.data.comment

import com.garnegsoft.hubs.data.Filter
import com.garnegsoft.hubs.data.HabrList
import com.garnegsoft.hubs.data.article.AbstractSnippetListModel
import com.garnegsoft.hubs.data.comment.list.CommentSnippet
import com.garnegsoft.hubs.data.comment.list.CommentsListController
import kotlinx.coroutines.CoroutineScope

class CommentsListModel(
        override val path: String,
        override val coroutineScope: CoroutineScope,
        vararg baseArgs: Pair<String, String>,
        initialFilter: Filter? = null,
) : AbstractSnippetListModel<CommentSnippet>(
        path = path,
        coroutineScope = coroutineScope,
        baseArgs = baseArgs.toMap(),
        initialFilter = initialFilter
) {
    override fun load(args: Map<String, String>): HabrList<CommentSnippet>? =
            CommentsListController.getCommentsSnippets(path, args)
}