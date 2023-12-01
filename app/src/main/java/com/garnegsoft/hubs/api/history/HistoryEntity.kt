package com.garnegsoft.hubs.api.history

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Timestamp
import java.util.Date

@Entity(tableName = "history")
data class HistoryEntity(
	
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