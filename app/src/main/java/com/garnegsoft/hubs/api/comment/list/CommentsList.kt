package com.garnegsoft.hubs.api.comment.list

import com.garnegsoft.hubs.api.HabrApi
import com.garnegsoft.hubs.api.HabrList
import com.garnegsoft.hubs.api.article.Article
import com.garnegsoft.hubs.api.comment.Comment
import com.garnegsoft.hubs.api.utils.formatTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import org.jsoup.Jsoup

class CommentsListController {
    companion object {
        private fun get(path: String, args: Map<String, String>? = null): CommentsList? {
            var response = HabrApi.get(path, args)
            var customJson = Json { ignoreUnknownKeys = true }
            if (response?.body != null) {
                var responseJson = Json.parseToJsonElement(response.body!!.string())
                var commentsList = customJson.decodeFromJsonElement<CommentsList>(responseJson)
                commentsList.comments.values.forEach {
                    it.apply {
                        timePublished = formatTime(timePublished)
                        author?.avatarUrl?.let {
                            author?.avatarUrl = "https:" + author?.avatarUrl
                        }
                    }
                }
                return commentsList
            }

            return null
        }

        fun getComments(path: String, args: Map<String, String>? = null): ArrayList<Comment> {
            var serializedData = get(path, args)

            var commentsList = ArrayList<Comment>()

            serializedData?.comments?.values?.forEach {
                commentsList.add(parseComment(it))
            }

            if (commentsList.size > 0) {
                var maxLevel = commentsList.sortedByDescending { it.parentCommentId }[0].level

                for (currentLevel in maxLevel downTo 1){
                    commentsList.filter { it.level == currentLevel }
                        .forEach { child ->
                            commentsList.find { parentComment ->
                                parentComment.id == child.parentCommentId
                            }?.children?.add(child)
                            commentsList.remove(child)
                        }
                }

            }

            return commentsList
        }

        private fun parseComment(comment: CommentsList.Comment): Comment {
            if (comment.editorVersion == 0){
                return Comment(
                    id = comment.id.toInt(),
                    level = comment.level,
                    author = Article.Author(
                        alias = "",
                        avatarUrl = null,
                        fullname = null
                    ),
                    publishedTime = comment.timePublished,
                    children = ArrayList<Comment>(),
                    deleted = true,
                    message = comment.message,
                    score = comment.score,
                    isArticleAuthor = comment.isPostAuthor,
                    parentCommentId = comment.parentId?.toInt(),
                    edited = comment.timeChanged != null,
                    isNew = comment.isNew ?: false,
                    isUserAuthor = comment.isAuthor,
                )
            }
            return Comment(
                id = comment.id.toInt(),
                level = comment.level,
                author = Article.Author(
                    alias = comment.author!!.alias!!,
                    avatarUrl = comment.author?.avatarUrl,
                    fullname = comment.author!!.fullname
                ),
                publishedTime = comment.timePublished,
                children = ArrayList<Comment>(),
                deleted = false,
                message = comment.message,
                score = comment.score,
                isArticleAuthor = comment.isPostAuthor,
                parentCommentId = comment.parentId?.toInt(),
                edited = comment.timeChanged != null,
                isNew = comment.isNew ?: false,
                isUserAuthor = comment.isAuthor,
            )
        }

        fun getCommentsSnippets(path: String, args: Map<String, String>? = null): HabrList<CommentSnippet>? {
            val raw = get(path, args)

            var result: HabrList<CommentSnippet>? = null

            var commentsList = arrayListOf<CommentSnippet>()

            raw?.let {
                raw.threads.forEach {
                    raw.comments[it]?.let{
                        commentsList.add(
                            CommentSnippet(
                                id = it.id.toInt(),
                                parentPost = CommentSnippet.ParentPost(
                                    id = it.post!!.id,
                                    title = it.post!!.title
                                ),
                                text = Jsoup.parse(it.message).text(),
                                timePublished = it.timePublished,
                                score = it.score,
                                author = Article.Author(
                                    alias = it.author!!.alias!!,
                                    fullname = it.author!!.fullname,
                                    avatarUrl = it.author!!.avatarUrl,
                                )
                            )
                        )
                    }
                }


                result = HabrList(commentsList, raw.pages!!)
            }

            return result
        }
    }

    @Serializable
    private data class CommentsList(
        val comments: Map<String, Comment>,
        val threads: ArrayList<String>,
        //val commentAccess: CommentAccess,
        val lastCommentTimestamp: Long? = null,
        val pages: Int? = null
    ) {
        @Serializable
        data class Comment(
            var id: String,
            var parentId: String? = null,
            var level: Int,
            var timePublished: String,
            var timeChanged: String? = null,
            var isSuspended: Boolean,
            //val status: Status,
            var score: Int,
            var votesCount: Int,
            var message: String,
            var editorVersion: Int,
            var author: Author? = null,
            var isAuthor: Boolean,
            var isPostAuthor: Boolean,
            var isNew: Boolean? = null,
            var isFavorite: Boolean? = null,
            var isCanEdit: Boolean? = null,
            //ral timeEditAllowedTill: Any? = null,
            var children: ArrayList<String>,
            //val vote: Vote? = null,
            var post: Post? = null,
            var isPinned: Boolean
        ) {
            @Serializable
            data class Post(
                var id: Int,
                var title: String,
                var commentsCount: Int,
                var postType: String,
                )

            @Serializable
            data class Author(
                var id: String? = null,
                var alias: String? = null,
                var fullname: String? = null,
                var avatarUrl: String? = null,
                var speciality: String? = null
            )

            @Serializable
            data class Access(
                val isCanComment: Boolean,
                val cantCommentReasonKey: String,
                val cantCommentReason: String,
                //val data: Any? = null
            )

            @Serializable
            data class Vote(
                //val value: Any? = null,
                val isCanVote: Boolean
            )
        }
    }


}