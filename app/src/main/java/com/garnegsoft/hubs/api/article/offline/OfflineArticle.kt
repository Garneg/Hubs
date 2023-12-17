package com.garnegsoft.hubs.api.article.offline

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import kotlinx.coroutines.flow.Flow


private const val SNIPPETS_TABLE_NAME = "offline_articles_snippets"
private const val ARTICLES_TABLE_NAME = "offline_articles"
private const val DATABASE_NAME = "offline_db"

@Entity(
	tableName = SNIPPETS_TABLE_NAME,
	indices = [Index("article_id", unique = true)]
)
data class OfflineArticleSnippet(
	
	@ColumnInfo("article_id")
	val articleId: Int,
	
	@ColumnInfo("author_name")
	val authorName: String?,
	
	@ColumnInfo("author_avatar_url")
	val authorAvatarUrl: String?,
	
	@ColumnInfo("time_published")
	val timePublished: String,
	
	val title: String,
	
	@ColumnInfo("reading_time")
	val readingTime: Int,
	
	@ColumnInfo("is_translation")
	val isTranslation: Boolean,
	
	@ColumnInfo("text_snippet")
	val textSnippet: String,
	
	@ColumnInfo("thumbnail_url")
	val thumbnailUrl: String?,
	
	@TypeConverters(HubsConverter::class)
	val hubs: HubsList,
	
	@ColumnInfo
	@PrimaryKey(autoGenerate = true)
	val id: Int = 0,
)

@Entity(
	tableName = ARTICLES_TABLE_NAME,
)
data class OfflineArticle(
	@ColumnInfo("article_id", index = true)
	val articleId: Int,
	
	@ColumnInfo("author_name")
	val authorName: String?,
	
	@ColumnInfo("author_avatar_url")
	val authorAvatarUrl: String?,
	
	@ColumnInfo("time_published")
	val timePublished: String,
	
	val title: String,
	
	@ColumnInfo("reading_time")
	val readingTime: Int,
	
	@ColumnInfo("is_translation")
	val isTranslation: Boolean,
	
	
	@ColumnInfo("content_html")
	val contentHtml: String,
	
	@TypeConverters(HubsConverter::class)
	val hubs: HubsList,
	
	@PrimaryKey(autoGenerate = true)
	val id: Int = 0,
	
	)

@ProvidedTypeConverter
class HubsConverter {
	
	@TypeConverter
	fun fromHubs(hubs: HubsList): String {
		return hubs.hubsList.joinToString(",")
	}
	
	@TypeConverter
	fun fromString(hubs: String): HubsList {
		return HubsList(hubs.split(","))
	}
	
	
}

class HubsList(
	val hubsList: List<String>
)

val Context.offlineArticlesDatabase: OfflineArticlesDatabase
	get() = OfflineArticlesDatabase.getDb(this)


@Dao
interface OfflineArticlesDao {
	
	/**
	 * @return article entity by **article_id**, not by room table id
	 */
	@Query("SELECT * FROM $ARTICLES_TABLE_NAME WHERE article_id = :articleId")
	fun getArticleById(articleId: Int): OfflineArticle
	
	@Query("SELECT EXISTS (SELECT * FROM $ARTICLES_TABLE_NAME WHERE article_id = :articleId)")
	fun exists(articleId: Int): Boolean
	
	@Query("SELECT EXISTS (SELECT * FROM $ARTICLES_TABLE_NAME WHERE article_id = :articleId)")
	fun existsFlow(articleId: Int): Flow<Boolean>
	
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insertSnippet(entity: OfflineArticleSnippet)
	
	@Delete
	fun deleteSnippet(entity: OfflineArticleSnippet)
	
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insert(entity: OfflineArticle)
	
	@Query("DELETE FROM $ARTICLES_TABLE_NAME WHERE article_id = :id")
	fun delete(id: Int)
	
	@Query("DELETE FROM $SNIPPETS_TABLE_NAME WHERE article_id = :id")
	fun deleteSnippet(id: Int)
	
	/**
	 * @return list of article entities from older to newer
	 */
	@Query("SELECT * FROM $SNIPPETS_TABLE_NAME ORDER BY id ASC")
	fun getAllSnippetsSortedByIdAsc(): Flow<List<OfflineArticleSnippet>>
	
	/**
	 * @return list of article entities from newer to older
	 */
	@Query("SELECT * FROM $SNIPPETS_TABLE_NAME ORDER BY id DESC")
	fun getAllSnippetsSortedByIdDesc(): Flow<List<OfflineArticleSnippet>>
	
}

@TypeConverters(HubsConverter::class)
@Database(
	entities = [OfflineArticleSnippet::class, OfflineArticle::class],
	version = 2
)
abstract class OfflineArticlesDatabase : RoomDatabase() {
	
	abstract fun articlesDao(): OfflineArticlesDao
	
	companion object {
		@Volatile
		private var instance: OfflineArticlesDatabase? = null
		fun getDb(context: Context): OfflineArticlesDatabase {
			return instance ?: synchronized(this) {
				val inst = Room.databaseBuilder(
					context = context,
					klass = OfflineArticlesDatabase::class.java,
					name = DATABASE_NAME
				)
					.addTypeConverter(HubsConverter())
					.fallbackToDestructiveMigration()
					.build()
				instance = inst
				inst
			}
		}
	}
}