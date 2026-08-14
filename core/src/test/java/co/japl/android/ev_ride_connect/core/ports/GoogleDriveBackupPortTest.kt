package co.japl.android.ev_ride_connect.core.ports

import co.japl.android.ev_ride_connect.core.domain.BackupConfig
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class GoogleDriveBackupPortTest {

    @Test
    fun shouldPerformManualBackupSuccessfully() = runTest {
        val port = FakeGoogleDriveBackupPort()
        val result = port.performManualBackup("/path/to/db.sqlite", listOf("/path/to/img1.png"))

        assertThat(result).isTrue()
        assertThat(port.executedDatabasePath).isEqualTo("/path/to/db.sqlite")
        assertThat(port.executedImagePaths).containsExactly("/path/to/img1.png")
    }

    @Test
    fun shouldConfigureAutomaticBackupSuccessfully() = runTest {
        val port = FakeGoogleDriveBackupPort()
        val config = BackupConfig(
            isAutoBackupEnabled = true,
            backupIntervalHours = 12,
            lastBackupTimestamp = 1000L,
            backupAppFolder = "appDataFolder"
        )

        val result = port.configureAutomaticBackup(config)

        assertThat(result).isTrue()
        assertThat(port.currentConfig).isEqualTo(config)
    }

    private class FakeGoogleDriveBackupPort : GoogleDriveBackupPort {
        var executedDatabasePath: String? = null
        var executedImagePaths: List<String> = emptyList()
        var currentConfig: BackupConfig = BackupConfig(false, 24, 0L, "appDataFolder")

        override suspend fun performManualBackup(databasePath: String, imagePaths: List<String>): Boolean {
            executedDatabasePath = databasePath
            executedImagePaths = imagePaths
            return true
        }

        override suspend fun configureAutomaticBackup(config: BackupConfig): Boolean {
            currentConfig = config
            return true
        }

        override suspend fun getBackupConfig(): BackupConfig {
            return currentConfig
        }

        override suspend fun performBackup(databasePath: String, imagePaths: List<String>): Boolean {
            return performManualBackup(databasePath, imagePaths)
        }
    }
}
