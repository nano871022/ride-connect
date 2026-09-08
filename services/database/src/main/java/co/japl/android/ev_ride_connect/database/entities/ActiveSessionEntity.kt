package co.japl.android.ev_ride_connect.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "active_session")
data class ActiveSessionEntity(
    @PrimaryKey
    val id: Long = 1L,
    @ColumnInfo(name = "is_ride_active")
    val isRideActive: Boolean = false,
    @ColumnInfo(name = "start_time_millis")
    val startTimeMillis: Long = 0L,
    @ColumnInfo(name = "current_duration_millis")
    val currentDurationMillis: Long = 0L,
    @ColumnInfo(name = "current_distance_km")
    val currentDistanceKm: Double = 0.0,
    @ColumnInfo(name = "pending_llm_prompt")
    val pendingLlmPrompt: String? = null,
    @ColumnInfo(name = "pending_llm_response")
    val pendingLlmResponse: String? = null,
    @ColumnInfo(name = "is_llm_processing")
    val isLlmProcessing: Boolean = false,
    @ColumnInfo(name = "cached_telemetry_count")
    val cachedTelemetryCount: Int = 0,
    @ColumnInfo(name = "last_updated_tmst")
    val lastUpdatedTmst: Long = System.currentTimeMillis()
)
