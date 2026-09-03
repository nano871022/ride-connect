package co.japl.android.ev_ride_connect.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(
    tableName = "ev_data",
    primaryKeys = ["ev_code", "km", "battery_level"]
)
data class EvDataEntity(
    @ColumnInfo(name = "ev_code")
    val evCode: String,
    @ColumnInfo(name = "km")
    val km: Long,
    @ColumnInfo(name = "battery_level")
    val batteryLevel: Short,
    @ColumnInfo(name = "crte_tmst")
    val createTmst: Long = System.currentTimeMillis()
)
