package com.garnegsoft.hubs.data.comment.offline

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

const val OFFLINE_COMMENTS_TABLE_NAME = "offline_comments"


@Entity(
	tableName = OFFLINE_COMMENTS_TABLE_NAME)
data class OfflineComment(
	@ColumnInfo("comment_id")
	val commentId: Int,
	@ColumnInfo("parent_comment_id")
	val parentCommentId: Int?,
	@ColumnInfo("level")
	val level: Int,
	@ColumnInfo("published_time")
	val publishedTime: String,
	@ColumnInfo("message")
	val message: String,
	@ColumnInfo("score")
	val score: Int,
	@ColumnInfo("parent_publication_id", index = true)
	val parentPublicationId: Int,
	@ColumnInfo("deleted")
	val deleted: Boolean,
	@ColumnInfo("is_author_of_publication")
	val isAuthorOfPublication: Boolean,
	@ColumnInfo("author_alias")
	val authorAlias: String,
	@ColumnInfo("author_avatar_url")
	val authorAvatarUrl: String,
	@PrimaryKey(autoGenerate = true)
	val id: Int = 0
)