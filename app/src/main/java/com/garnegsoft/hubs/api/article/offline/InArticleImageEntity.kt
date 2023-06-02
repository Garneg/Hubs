package com.garnegsoft.hubs.api.article.offline

import androidx.room.*


@Entity(
    tableName = "in_article_images",
)
data class InArticleImageEntity(

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    val data: ByteArray,

    @ColumnInfo(name = "original_url", index = true)
    val originalUrl: String,

    @ColumnInfo(name = "parent_article_id", index = true)
    val parentArticleId: Int,

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
)

@Dao
interface InArticleImagesDao {

    @Upsert
    fun upsert(entity: InArticleImageEntity)

    @Query("DELETE FROM in_article_images WHERE parent_article_id = :articleId")
    fun deleteAllBelongsToArticle(articleId: Int)

    @Delete
    fun delete(entity: InArticleImageEntity)

    @Query("SELECT * FROM in_article_images WHERE parent_article_id = :articleId")
    fun getAllBelongsToArticle(articleId: Int): List<InArticleImageEntity>

    @Query("SELECT * FROM in_article_images WHERE original_url = :url LIMIT 1")
    fun getByUrl(url: String): InArticleImageEntity

}

