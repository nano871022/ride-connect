package co.japl.android.ev_ride_connect.interfaces.model

data class BackupConfig(
    val isAutoBackupEnabled: Boolean = false,
    val backupIntervalHours: Int = 24,
    val lastBackupTimestampMs: Long = 0L,
    val backupAppFolder: String = "AppSpace",
    val lastBackupTimestamp: Long = lastBackupTimestampMs
)
