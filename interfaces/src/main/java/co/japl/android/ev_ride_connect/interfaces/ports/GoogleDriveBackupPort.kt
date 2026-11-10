package co.japl.android.ev_ride_connect.interfaces.ports

import co.japl.android.ev_ride_connect.interfaces.model.BackupConfig

interface GoogleDriveBackupPort {
    suspend fun getBackupConfig(): BackupConfig
    suspend fun configureAutomaticBackup(config: BackupConfig): Boolean
    suspend fun performManualBackup(databasePath: String, imagePaths: List<String>): Boolean
    suspend fun performBackup(databasePath: String, imagePaths: List<String>): Boolean
}
