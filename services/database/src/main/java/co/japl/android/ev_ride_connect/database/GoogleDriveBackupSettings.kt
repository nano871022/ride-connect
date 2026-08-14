package co.japl.android.ev_ride_connect.database

object GoogleDriveBackupSettings {
    val ACTION_BACKUP_NOW = "${GoogleDriveBackupHelper::class.java.name}.ACTION_BACKUP_NOW"
    val ACTION_SCHEDULE_BACKUP = "${GoogleDriveBackupHelper::class.java.name}.ACTION_SCHEDULE_BACKUP"
    val KEY_BACKUP_INTERVAL_HOURS = "${GoogleDriveBackupHelper::class.java.name}.KEY_BACKUP_INTERVAL_HOURS"
    val KEY_AUTO_BACKUP_ENABLED = "${GoogleDriveBackupHelper::class.java.name}.KEY_AUTO_BACKUP_ENABLED"
    val STATUS_SUCCESS = "${GoogleDriveBackupHelper::class.java.name}.STATUS_SUCCESS"
    val STATUS_FAILURE = "${GoogleDriveBackupHelper::class.java.name}.STATUS_FAILURE"
    val DRIVE_APP_FOLDER = "appDataFolder"
}
