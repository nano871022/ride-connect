package co.japl.android.ev_ride_connect.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "distance")
    val distance: Int = 0,
    @ColumnInfo(name = "battery_consumed")
    val batteryConsumed: Int = 0,
    @ColumnInfo(name = "duration")
    val duration: Long = 0,
    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "time_trip")
    val timeTrip: Long = 0,
    @ColumnInfo(name = "average_speed")
    val averageSpeed: Double = 0.0,
    @ColumnInfo(name = "distance_km")
    val distanceKm: Double = 0.0,
    @ColumnInfo(name = "create_tmst")
    val createTmst: Long = System.currentTimeMillis()
)
