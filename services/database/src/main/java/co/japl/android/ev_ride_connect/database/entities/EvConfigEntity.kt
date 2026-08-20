package co.japl.android.ev_ride_connect.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ev_configs")
data class EvConfigEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "request")
    val request: String = "",
    @ColumnInfo(name = "brand")
    val brand: String = "",
    @ColumnInfo(name = "version")
    val version: String = "",
    @ColumnInfo(name = "motors_json")
    val motorsJson: String = "",
    @ColumnInfo(name = "manufactory_year")
    val manufactoryYear: String = "",
    @ColumnInfo(name = "manufactory_company")
    val manufactoryCompany: String = "",
    @ColumnInfo(name = "bought_date")
    val boughtDate: String = "",
    @ColumnInfo(name = "battery_technology")
    val batteryTechnology: String = "",
    @ColumnInfo(name = "battery_volts")
    val batteryVolts: String = "",
    @ColumnInfo(name = "battery_ampers")
    val batteryAmpers: String = "",
    @ColumnInfo(name = "brake_quantity")
    val brakeQuantity: Int = 0,
    @ColumnInfo(name = "brake_technology")
    val brakeTechnology: String = "",
    @ColumnInfo(name = "suspension_technology")
    val suspensionTechnology: String = "",
    @ColumnInfo(name = "charge_power")
    val chargePower: String = "",
    @ColumnInfo(name = "other_characteristics")
    val otherCharacteristics: String = "",
    @ColumnInfo(name = "is_loaded")
    val isLoaded: Boolean = false
)
