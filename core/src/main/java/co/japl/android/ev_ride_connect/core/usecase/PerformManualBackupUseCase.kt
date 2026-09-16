package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.core.ports.GoogleDriveBackupPort
import javax.inject.Inject

class PerformManualBackupUseCase @Inject constructor(
    private val backupPort: GoogleDriveBackupPort
) {
    suspend fun execute(databasePath: String = "", imagePaths: List<String> = emptyList()): Boolean =
        backupPort.performManualBackup(databasePath, imagePaths)
}
