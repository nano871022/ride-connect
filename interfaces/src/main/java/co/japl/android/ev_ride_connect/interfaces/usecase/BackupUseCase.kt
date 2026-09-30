package co.japl.android.ev_ride_connect.interfaces.usecase

import co.japl.android.ev_ride_connect.interfaces.model.BackupConfig

interface BackupUseCase {
    suspend fun getBackupConfig(): BackupConfig
    suspend fun performManualBackup(databasePath: String = "", imagePaths: List<String> = emptyList()): Boolean
    suspend fun configureAutoBackup(config: BackupConfig): Boolean
}
