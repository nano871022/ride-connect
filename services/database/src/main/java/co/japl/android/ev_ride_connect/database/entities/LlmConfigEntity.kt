package co.japl.android.ev_ride_connect.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "llm_configs")
data class LlmConfigEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "model_name")
    val modelName: String,
    @ColumnInfo(name = "selected_version")
    val selectedVersion: String = "",
    @ColumnInfo(name = "api_key")
    val apiKey: String,
    @ColumnInfo(name = "created_at")
    val createdAt: String,
    @ColumnInfo(name = "updated_at")
    val updatedAt: String,
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true
)
