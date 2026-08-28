package co.japl.android.ev_ride_connect.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "trip_gps",
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["trip_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["trip_id"])]
)
data class TripGpsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "trip_id")
    val tripId: Long,
    @ColumnInfo(name = "order_index")
    val orderIndex: Int,
    @ColumnInfo(name = "speed")
    val speed: Double,
    @ColumnInfo(name = "distance")
    val distance: Double,
    @ColumnInfo(name = "x")
    val x: Double,
    @ColumnInfo(name = "y")
    val y: Double,
    @ColumnInfo(name = "create_tmst")
    val createTmst: Long = System.currentTimeMillis()
)
