package com.garnegsoft.hubs.api.comment.list

import com.garnegsoft.hubs.api.HabrApi
import com.garnegsoft.hubs.api.HabrDataParser
import com.garnegsoft.hubs.api.HabrList
import com.garnegsoft.hubs.api.article.Article
import com.garnegsoft.hubs.api.comment.CommentsCollection
import com.garnegsoft.hubs.api.comment.Comment
import com.garnegsoft.hubs.api.comment.ThreadSnippet
import com.garnegsoft.hubs.api.comment.Threads
import com.garnegsoft.hubs.api.utils.formatTime
import com.garnegsoft.hubs.api.utils.placeholderAvatarUrl
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
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
                        author?.let {
                            if (it.avatarUrl == null) {
                                it.alias?.let {
                                    author!!.avatarUrl = placeholderAvatarUrl(it)
                                    
                                }
                            } else {
                                it.avatarUrl = "https:" + author?.avatarUrl
                            }
                        }
                    }
                }
                commentsList.moderated?.values?.forEach {
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

        fun getComments(articleId: Int): CommentsCollection? {
            return getComments("articles/$articleId/comments")
        }

        fun getComments(path: String, args: Map<String, String>? = null): CommentsCollection? {
            var serializedData = get(path, args)

            var commentsList = ArrayList<Comment>()


            serializedData?.comments?.values?.forEach {
                commentsList.add(parseComment(it, false))
            }
            serializedData?.moderated?.values?.forEach {
                commentsList.add(parseComment(it, true))
            }


            if (commentsList.size > 0) {

                var maxLevel = commentsList.maxByOrNull { it.level }?.level ?: 0

                commentsList.sortBy { it.level }
                var newList = ArrayList<Comment>()

                for (level in 0..maxLevel + 1) {
                    commentsList.forEach {
                        if (it.level == level) {
                            var nextIndex = newList.size
                            if (newList.find { it1 -> it1.id == it.id } == null) {
                                newList.add(it)
                                nextIndex++
                            } else {
                                nextIndex =
                                    newList.indexOf(newList.find { it1 -> it1.id == it.id }) + 1
                            }
                            newList.addAll(
                                nextIndex,
                                commentsList.filter { it1 -> it1.parentCommentId == it.id })
                        }
                    }
                }
                
                

                return CommentsCollection(
                    comments = newList,
                    commentAccess = CommentsCollection.CommentAccess(
                        canComment = serializedData?.commentAccess?.isCanComment ?: false,
                        cantCommentReason = serializedData?.commentAccess?.cantCommentReason
                    ),
                    pinnedComments = serializedData?.pinnedCommentIds?.map { it.toInt() } ?: emptyList()
                )
            }

            return CommentsCollection(
                comments = commentsList,
                commentAccess = CommentsCollection.CommentAccess(
                    canComment = serializedData?.commentAccess?.isCanComment ?: false,
                    cantCommentReason = serializedData?.commentAccess?.cantCommentReason
                ),
                pinnedComments = serializedData?.pinnedCommentIds?.map { it.toInt() } ?: emptyList()
            )
        }
        
        fun getThreads(articleId: Int): Threads? {
            val comments = getComments(articleId)
            return comments?.let {
                val threads = arrayListOf<ThreadSnippet>()
                var currentRootComment: Comment? = null
                var threadCommentsCount = 0
                var newCommentsCount = 0
                it.comments.forEach {
                    if (it.level == 0){
                        
                        currentRootComment?.let {
                            threads.add(
                                ThreadSnippet(
                                    root = it,
                                    threadChildrenCommentsCount = threadCommentsCount,
                                    threadNewChildrenCommentsCount = newCommentsCount
                                    )
                            )
                        }
                        
                        threadCommentsCount = 0
                        currentRootComment = it
                        newCommentsCount = 0
                    } else {
                        threadCommentsCount++
                        if (it.isNew)
                            newCommentsCount++
                        
                    }
                    
                    
                }
                currentRootComment?.let {
                    threads.add(
                        ThreadSnippet(
                            root = it,
                            threadChildrenCommentsCount = threadCommentsCount,
                            threadNewChildrenCommentsCount = newCommentsCount
                        )
                    )
                }
                
                Threads(threads, it.commentAccess)
            }
        }
        
        fun getThread(articleId: Int, threadId: Int): CommentsCollection? {
            val comments = getComments(articleId)
            
            return comments?.let {
                val newComments = arrayListOf<Comment>()
                var doConcatinating = false
                it.comments.forEach {
                    if (it.level == 0) {
                        if (it.id == threadId){
                            doConcatinating = true
                        }
                        else {
                            doConcatinating = false
                        }
                    }
                    if (doConcatinating) newComments.add(it)
                }
                CommentsCollection(newComments, it.commentAccess, it.pinnedComments)
            }
        }

        private fun parseComment(comment: CommentsList.Comment, inModeration: Boolean): Comment {
            if (comment.editorVersion == 0) {
                return Comment(
                    id = comment.id.toInt(),
                    level = comment.level,
                    author = Article.Author(
                        alias = "",
                        avatarUrl = null,
                        fullname = null
                    ),
                    publishedTime = comment.timePublished,
                    deleted = true,
                    message = comment.message,
                    score = comment.score,
                    votesCount = comment.votesCount,
                    isArticleAuthor = comment.isPostAuthor,
                    parentCommentId = comment.parentId?.toInt(),
                    edited = comment.timeChanged != null,
                    isNew = comment.isNew ?: false,
                    isUserAuthor = comment.isAuthor,
                    inModeration = inModeration
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
                deleted = false,
                message = comment.message,
                score = comment.score,
                votesCount = comment.votesCount,
                isArticleAuthor = comment.isPostAuthor,
                parentCommentId = comment.parentId?.toInt(),
                edited = comment.timeChanged != null,
                isNew = comment.isNew ?: false,
                isUserAuthor = comment.isAuthor,
                inModeration = inModeration,
            )
        }

        fun getCommentsSnippets(
            path: String,
            args: Map<String, String>? = null
        ): HabrList<CommentSnippet>? {
            val raw = get(path, args)

            var result: HabrList<CommentSnippet>? = null

            var commentsList = arrayListOf<CommentSnippet>()

            raw?.let {
                raw.threads.forEach {
                    raw.comments[it]?.let {
                        if (it.post != null || it.publication != null) {
                            commentsList.add(
                                CommentSnippet(
                                    id = it.id.toInt(),
                                    parentPost = CommentSnippet.ParentPost(
                                        id = it.publication?.id?.toInt() ?: it.post!!.id,
                                        title = Jsoup.parse(it.publication!!.title).text()
                                    ),
                                    text = it.message,
                                    timePublished = it.timePublished,
                                    score = it.score ?: 0,
                                    author = Article.Author(
                                        alias = it.author!!.alias!!,
                                        fullname = it.author!!.fullname,
                                        avatarUrl = it.author!!.avatarUrl,
                                    )
                                )
                            )
                        }
                    }
                }


                result = HabrList(commentsList, raw.pages!!)
            }

            return result
        }

        private fun _sendComment(
            articleId: Int,
            text: String,
            parentCommentId: Int? = null
        ): SendCommentResponse? {

            val comment = UserCommentPayload(
                articleId = articleId.toString(),
                parentId = parentCommentId?.toString(),
                isPost = false,
                text = UserCommentPayload.CommentContent(
                    source = text,
                    editorVersion = 2,
                    isMarkdown = true
                )
            )

            val serializedComment = Json.encodeToString(comment)

            val response = HabrApi.post(
                "comments/posts/$articleId/add",
                requestBody = serializedComment.toRequestBody(contentType = "application/json".toMediaType())
            )
            if (response?.code != 200)
                return null

            response.body?.string()?.let {
                val sendResponse = HabrDataParser.parseJson<SendCommentResponse>(it)
                return sendResponse
            }

            return null
        }

        fun sendComment(
            articleId: Int,
            text: String,
            parentCommentId: Int? = null
        ): CommentsCollection.CommentAccess? {
            val response = _sendComment(articleId, text, parentCommentId)
            return response?.let {
                CommentsCollection.CommentAccess(
                    response.commentAccess.isCanComment,
                    response.commentAccess.cantCommentReason,
                )
            }
        }
    }


    @Serializable
    private data class UserCommentPayload(
        var articleId: String,
        var text: CommentContent,
        var parentId: String?,
        var isPost: Boolean
    ) {
        @Serializable
        data class CommentContent(
            var source: String,
            var editorVersion: Long,
            var isMarkdown: Boolean
        )
    }

    @Serializable
    private data class SendCommentResponse(
        val commentAccess: CommentsList.CommentAccess,
        val data: CommentsList.Comment
    )


    @Serializable
    private data class CommentsList(
        val comments: Map<String, Comment>,
        val threads: ArrayList<String>,
        var moderated: Map<String, Comment>? = null,
        var commentAccess: CommentAccess? = null,
        val lastCommentTimestamp: Long? = null,
        val pages: Int? = null,
        val pinnedCommentIds: List<String>? = null
    ) {
        @Serializable
        data class Comment(
            var id: String,
            var parentId: String? = null,
            var level: Int,
            var timePublished: String,
            var timeChanged: String? = null,
            var isSuspended: Boolean?,
            //val status: Status,
            var score: Int?,
            var votesCount: Int?,
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
            var publication: Post? = null,
            var post: LegacyPost? = null,
            var isPinned: Boolean
        ) {
            
            @Serializable
            data class LegacyPost(
                var id: Int,
                var title: String
            )
            
            @Serializable
            data class Post(
                var id: String,
                var publicationType: String,
                var title: String,
                var isCorporative: Boolean
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
            data class Vote(
                //val value: Any? = null,
                val isCanVote: Boolean
            )
        }

        @Serializable
        data class CommentAccess(
            val isCanComment: Boolean,
            val cantCommentReasonKey: String?,
            val cantCommentReason: String?,
//            val data: Any? = null
        )
    }


}