package com.garnegsoft.hubs.api.history

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert
import com.garnegsoft.hubs.api.HabrSnippet
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Calendar
import kotlin.math.ceil

@Entity(tableName = "history")
data class HistoryEntity(
	
	/**
	 * Data in json about the event depending on type.
	 */
	@ColumnInfo("data")
	val data: String,
	
	@ColumnInfo("action_type")
	val actionType: HistoryActionType,
	
	@ColumnInfo("timestamp")
	val timestamp: Long,
	
	@PrimaryKey(autoGenerate = true)
	override val id: Int = 0
) : HabrSnippet

enum class HistoryActionType {
	Undefined,
	Article,
	UserProfile,
	HubProfile,
	CompanyProfile,
	Comments
}


@Dao
interface HistoryDao {
	
	@Upsert
	fun insertEvent(event: HistoryEntity): Long
	
	@Query("SELECT * FROM history ORDER BY timestamp DESC LIMIT :eventsPerPage OFFSET :pageIndex * :eventsPerPage")
	fun getEventsPaged(pageIndex: Int, eventsPerPage: Int = 20): List<HistoryEntity>
	
	@Delete
	fun deleteEvent(event: HistoryEntity)
	
	@Query("DELETE FROM history")
	fun clearAll()
	
	@Query("SELECT COUNT(id) FROM history")
	fun eventsCount(): Int
	
	fun pagesCount(eventsPerPage: Int = 20): Int {
		return ceil(eventsCount().toFloat() / eventsPerPage).toInt()
	}
}

@Serializable
sealed class HistoryType() {
	@get:Ignore
	abstract val actionType: HistoryActionType
	fun toHistoryEntity(timestamp: Long = Calendar.getInstance().time.time) : HistoryEntity {
		val data = Json.encodeToString(this)
		return HistoryEntity(data, actionType, timestamp)
	}
}

@Serializable
data class HistoryArticle(
	val articleId: Int,
	val title: String,
	val authorAlias: String,
	val authorAvatarUrl: String,
	val thumbnailUrl: String?,
) : HistoryType() {
	override val actionType: HistoryActionType = HistoryActionType.Article
}

@Serializable
data class HistoryUser(
	val alias: String,
	val avatarUrl: String,
) : HistoryType() {
	override val actionType = HistoryActionType.UserProfile
}

@Serializable
data class HistoryHub(
	val alias: String,
	val avatarUrl: String,
) : HistoryType() {
	override val actionType = HistoryActionType.HubProfile
}

@Serializable
data class HistoryCompany(
	val alias: String,
	val avatarUrl: String?,
) : HistoryType() {
	override val actionType = HistoryActionType.CompanyProfile
}

@Serializable
data class HistoryComments(
	val parentArticle: HistoryArticle,
) : HistoryType() {
	override val actionType = HistoryActionType.Comments
}


private val json = Json { ignoreUnknownKeys = true }

fun HistoryEntity.getArticle() : HistoryArticle {
	return json.decodeFromString(data)
}

fun HistoryEntity.getUser() : HistoryUser {
	return json.decodeFromString(data)
}

fun HistoryEntity.getHub() : HistoryHub {
	return json.decodeFromString(data)
}

fun HistoryEntity.getCompany() : HistoryCompany {
	return json.decodeFromString(data)
}

fun HistoryEntity.getComments() : HistoryComments {
	return json.decodeFromString(data)
}


@Database(entities = [HistoryEntity::class], version = 1)
abstract class HistoryDatabase : RoomDatabase() {
	
	abstract fun dao(): HistoryDao
	
	companion object {
		@Volatile
		private var instance: HistoryDatabase? = null
		fun getDb(context: Context): HistoryDatabase {
			return instance ?: synchronized(this){
				val inst = Room.databaseBuilder(
					context = context,
					klass = HistoryDatabase::class.java,
					name = "history"
				).build()
				instance = inst
				inst
			}
		}
	}
}