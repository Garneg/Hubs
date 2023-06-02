package com.garnegsoft.hubs.api.article.offline

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow


private const val TABLE_NAME = "offline_articles"

@Entity(
    tableName = TABLE_NAME,
    indices = [Index("article_id", unique = true),]
)
data class ArticleEntity(

    @ColumnInfo("article_id")
    val articleId: Int,

    @ColumnInfo("author_name")
    val authorName: String?,

    @ColumnInfo("author_avatar_data", typeAffinity = ColumnInfo.BLOB)
    val authorAvatarBase64: ByteArray?,

    @ColumnInfo("time_published")
    val timePublished: String,

    val title: String,

    @ColumnInfo("reading_time")
    val readingTime: Int,

    @ColumnInfo("is_translation")
    val isTranslation: Boolean,

    @ColumnInfo("content_html")
    val contentHtml: String,

    @ColumnInfo("thumbnail_image_data", typeAffinity = ColumnInfo.BLOB)
    val thumbnailImageData: ByteArray?,

    @ColumnInfo
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
)


val Context.offlineArticlesDatabase: OfflineArticlesDatabase
    get() = OfflineArticlesDatabase.getDb(this)


@Dao
interface OfflineArticlesDao{

    /**
     * @return article entity by **article_id**, not just by id
     */
    @Query("SELECT * FROM $TABLE_NAME WHERE article_id = :articleId")
    suspend fun _getArticleById(articleId: Int): ArticleEntity

    @Query("SELECT EXISTS (SELECT * FROM $TABLE_NAME WHERE article_id = :articleId)")
    suspend fun exists(articleId: Int): Boolean

    @Upsert
    suspend fun upsert(entity: ArticleEntity)

    @Delete
    suspend fun delete(entity: ArticleEntity)

    /**
     * @return list of article entities from older to newer
     */
    @Query("SELECT * FROM $TABLE_NAME ORDER BY id ASC")
    fun getAllSortedByIdAsc(): Flow<List<ArticleEntity>>

    /**
     * @return list of article entities from newer to older
     */
    @Query("SELECT * FROM $TABLE_NAME ORDER BY id DESC")
    fun getAllSortedByIdDesc(): Flow<List<ArticleEntity>>

}

@Database(entities = [ArticleEntity::class, InArticleImageEntity::class], version = 1)
abstract class OfflineArticlesDatabase : RoomDatabase() {

    abstract fun articlesDao(): OfflineArticlesDao
    abstract fun imagesDao(): InArticleImagesDao

    companion object {
        @Volatile
        private var instance: OfflineArticlesDatabase? = null
        fun getDb(context: Context): OfflineArticlesDatabase {
            return instance ?: synchronized(this){
                val inst = Room.databaseBuilder(
                    context = context,
                    klass = OfflineArticlesDatabase::class.java,
                    name = ""
                ).build()
                instance = inst
                inst
            }
        }
    }
}