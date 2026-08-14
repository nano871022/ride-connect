package co.japl.android.ev_ride_connect.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val distance: Int,
    val batteryConsumed: Int,
    val duration: Long = 0,
    val timestamp: Long = System.currentTimeMillis()
)
