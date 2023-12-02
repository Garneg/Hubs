package com.garnegsoft.hubs.api.history

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import java.sql.Timestamp
import java.util.Date

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
	val id: Int = 0
)

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
	fun insertEvent(event: HistoryEntity)
	
	@Query("SELECT * FROM history ORDER BY timestamp DESC LIMIT :eventsPerPage OFFSET :pageNumber * :eventsPerPage")
	fun getEventsPaged(pageNumber: Int, eventsPerPage: Int = 20)
	
	@Delete
	fun deleteEvent(event: HistoryEntity)
	
	@Query("DELETE FROM history")
	fun clearAll()
	
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
					name = "HISTORY"
				).build()
				instance = inst
				inst
			}
		}
	}
}