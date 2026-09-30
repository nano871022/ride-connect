package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.interfaces.model.BackupConfig
import co.japl.android.ev_ride_connect.interfaces.ports.GoogleDriveBackupPort
import co.japl.android.ev_ride_connect.interfaces.usecase.BackupUseCase
import javax.inject.Inject

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
