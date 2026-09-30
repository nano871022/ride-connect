package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.core.domain.BackupConfig
import co.japl.android.ev_ride_connect.core.ports.GoogleDriveBackupPort
import javax.inject.Inject

class BackupUseCase @Inject constructor(
    private val googleDriveBackupPort: GoogleDriveBackupPort,
    private val getBackupConfigUseCase: GetBackupConfigUseCase,
    private val performManualBackupUseCase: PerformManualBackupUseCase,
    private val configureAutoBackupUseCase: ConfigureAutoBackupUseCase
) {
    suspend fun getBackupConfig(): BackupConfig {
        return getBackupConfigUseCase.execute()
    }

    suspend fun performManualBackup(databasePath: String = "", imagePaths: List<String> = emptyList()): Boolean {
        return performManualBackupUseCase.execute(databasePath, imagePaths)
    }

    suspend fun configureAutoBackup(config: BackupConfig): Boolean {
        return configureAutoBackupUseCase.execute(config)
    }
}
