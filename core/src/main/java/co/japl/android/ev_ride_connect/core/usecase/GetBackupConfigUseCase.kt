package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.core.domain.BackupConfig
import co.japl.android.ev_ride_connect.core.ports.GoogleDriveBackupPort
import javax.inject.Inject

class GetBackupConfigUseCase @Inject constructor(
    private val backupPort: GoogleDriveBackupPort
) {
    suspend fun execute(): BackupConfig = backupPort.getBackupConfig()
}
