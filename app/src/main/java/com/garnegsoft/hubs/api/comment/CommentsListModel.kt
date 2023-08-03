package com.garnegsoft.hubs.api.comment

import com.garnegsoft.hubs.api.HabrList
import com.garnegsoft.hubs.api.article.AbstractSnippetListModel
import com.garnegsoft.hubs.api.comment.list.CommentSnippet
import com.garnegsoft.hubs.api.comment.list.CommentsListController
import kotlinx.coroutines.CoroutineScope

class CommentsListModel(
        override val path: String,
        override val coroutineScope: CoroutineScope,
        vararg baseArgs: Pair<String, String>,
        initialFilter: Map<String, String> = emptyMap()
) : AbstractSnippetListModel<CommentSnippet>(
        path = path,
        coroutineScope = coroutineScope,
        baseArgs = baseArgs.toMap(),
        initialFilter = initialFilter
) {
    override fun load(args: Map<String, String>): HabrList<CommentSnippet>? =
            CommentsListController.getCommentsSnippets(path, args)
}