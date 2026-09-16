package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.core.domain.BackupConfig
import co.japl.android.ev_ride_connect.core.ports.GoogleDriveBackupPort
import javax.inject.Inject

class ConfigureAutoBackupUseCase @Inject constructor(
    private val backupPort: GoogleDriveBackupPort
) {
    suspend fun execute(config: BackupConfig): Boolean =
        backupPort.configureAutomaticBackup(config)
}
