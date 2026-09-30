package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.core.domain.BackupConfig
import co.japl.android.ev_ride_connect.core.ports.GoogleDriveBackupPort
import javax.inject.Inject

interface BackupUseCase {
    suspend fun getBackupConfig(): BackupConfig
    suspend fun performManualBackup(databasePath: String = "", imagePaths: List<String> = emptyList()): Boolean
    suspend fun configureAutoBackup(config: BackupConfig): Boolean
}

class BackupUseCaseImpl @Inject constructor(
    private val googleDriveBackupPort: GoogleDriveBackupPort,
    private val getBackupConfigUseCase: GetBackupConfigUseCase,
    private val performManualBackupUseCase: PerformManualBackupUseCase,
    private val configureAutoBackupUseCase: ConfigureAutoBackupUseCase
) : BackupUseCase {
    override suspend fun getBackupConfig(): BackupConfig {
        return getBackupConfigUseCase.execute()
    }

    override suspend fun performManualBackup(databasePath: String, imagePaths: List<String>): Boolean {
        return performManualBackupUseCase.execute(databasePath, imagePaths)
    }

    override suspend fun configureAutoBackup(config: BackupConfig): Boolean {
        return configureAutoBackupUseCase.execute(config)
    }
}
