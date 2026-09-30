package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.interfaces.model.BackupConfig
import co.japl.android.ev_ride_connect.interfaces.ports.GoogleDriveBackupPort
import javax.inject.Inject

class ConfigureAutoBackupUseCase @Inject constructor(
    private val backupPort: GoogleDriveBackupPort
) {
    suspend fun execute(config: BackupConfig): Boolean =
        backupPort.configureAutomaticBackup(config)
}
