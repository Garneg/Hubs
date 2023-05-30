package com.garnegsoft.hubs.api.article.offline

import android.content.Context
import androidx.room.*
import androidx.room.util.TableInfo
import com.garnegsoft.hubs.api.article.Article
import kotlinx.coroutines.flow.Flow


private const val TABLE_NAME = "offline_articles"

@Entity(
    tableName = TABLE_NAME,
    indices = [Index(name = "id", unique = true)]
)
data class ArticleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo("article_id")
    val articleId: Int,

    @ColumnInfo("author_name")
    val authorName: String?,

    @ColumnInfo("author_avatar_base64")
    val authorAvatarBase64: String?,

    @ColumnInfo("content_html")
    val contentHtml: String,

    @ColumnInfo("thumbnail_image_base64")
    val thumbnailImageBase64: String?,

)

@Dao
interface OfflineArticlesDao{

    /**
     * @return article entity by **article_id**, not just by id
     */
    @Query("SELECT * FROM $TABLE_NAME WHERE article_id = :id")
    suspend fun getArticleById(id: Int): ArticleEntity

    @Upsert
    suspend fun upsert(entity: ArticleEntity)

    @Delete
    suspend fun delete(entity: ArticleEntity)

    /**
     * @return list of article entities from older to newer
     */
    @Query("SELECT * FROM $TABLE_NAME BY id ASC")
    fun getAllSortedByIdAsc(): Flow<List<ArticleEntity>>

    /**
     * @return list of article entities from newer to older
     */
    @Query("SELECT * FROM $TABLE_NAME BY id DESC")
    fun getAllSortedByIdDesc(): Flow<List<ArticleEntity>>

}

@Database(entities = [ArticleEntity::class], version = 1)
abstract class OfflineArticlesDatabase : RoomDatabase() {

    abstract fun dao(): OfflineArticlesDao

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