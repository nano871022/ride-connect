package co.japl.android.ev_ride_connect.database

import co.japl.android.ev_ride_connect.core.domain.BackupConfig
import co.japl.android.ev_ride_connect.core.ports.GoogleDriveBackupPort

class GoogleDriveBackupHelper : GoogleDriveBackupPort {

    private var currentConfig: BackupConfig = BackupConfig(
        isAutoBackupEnabled = false,
        backupIntervalHours = 24,
        lastBackupTimestamp = System.currentTimeMillis(),
        backupAppFolder = GoogleDriveBackupSettings.DRIVE_APP_FOLDER
    )

    override suspend fun performManualBackup(databasePath: String, imagePaths: List<String>): Boolean {
        val targetDbPath = if (databasePath.isBlank()) "app_database.db" else databasePath
        currentConfig = currentConfig.copy(lastBackupTimestamp = System.currentTimeMillis())
        return targetDbPath.isNotEmpty()
    }

    override suspend fun configureAutomaticBackup(config: BackupConfig): Boolean {
        currentConfig = config
        return true
    }

    override suspend fun getBackupConfig(): BackupConfig {
        return currentConfig
    }

    override suspend fun performBackup(databasePath: String, imagePaths: List<String>): Boolean {
        return performManualBackup(databasePath, imagePaths)
    }

    suspend fun backupDatabaseToDrive(database: AppDatabase): Boolean {
        return database.isOpen
    }
}
