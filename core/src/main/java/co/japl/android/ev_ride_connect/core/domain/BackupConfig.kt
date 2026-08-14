package co.japl.android.ev_ride_connect.core.domain

data class BackupConfig(
    val isAutoBackupEnabled: Boolean = false,
    val backupIntervalHours: Int = 24,
    val lastBackupTimestamp: Long = 0L,
    val backupAppFolder: String = "appDataFolder"
)
