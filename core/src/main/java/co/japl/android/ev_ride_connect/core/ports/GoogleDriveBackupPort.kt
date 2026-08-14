package co.japl.android.ev_ride_connect.core.ports

import co.japl.android.ev_ride_connect.core.domain.BackupConfig

interface GoogleDriveBackupPort {
    suspend fun performManualBackup(databasePath: String, imagePaths: List<String>): Boolean
    suspend fun configureAutomaticBackup(config: BackupConfig): Boolean
    suspend fun getBackupConfig(): BackupConfig
    suspend fun performBackup(databasePath: String, imagePaths: List<String>): Boolean
}
