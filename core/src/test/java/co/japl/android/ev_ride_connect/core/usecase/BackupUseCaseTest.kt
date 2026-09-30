package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.core.domain.BackupConfig
import co.japl.android.ev_ride_connect.core.ports.GoogleDriveBackupPort
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class BackupUseCaseTest {

    private lateinit var fakeBackupPort: FakeGoogleDriveBackupPort
    private lateinit var useCase: BackupUseCase

    @Before
    fun setUp() {
        fakeBackupPort = FakeGoogleDriveBackupPort()
        useCase = BackupUseCaseImpl(
            fakeBackupPort,
            GetBackupConfigUseCase(fakeBackupPort),
            PerformManualBackupUseCase(fakeBackupPort),
            ConfigureAutoBackupUseCase(fakeBackupPort)
        )
    }

    @Test
    fun shouldGetBackupConfig() = runTest {
        val config = useCase.getBackupConfig()
        assertThat(config.backupAppFolder).isEqualTo("testFolder")
    }

    @Test
    fun shouldPerformManualBackup() = runTest {
        val success = useCase.performManualBackup("db/path", listOf("img/path"))
        assertThat(success).isTrue()
        assertThat(fakeBackupPort.manualBackupCalled).isTrue()
    }

    @Test
    fun shouldConfigureAutoBackup() = runTest {
        val newConfig = BackupConfig(isAutoBackupEnabled = true, backupIntervalHours = 12)
        val success = useCase.configureAutoBackup(newConfig)
        assertThat(success).isTrue()
        assertThat(fakeBackupPort.currentConfig.isAutoBackupEnabled).isTrue()
    }

    private class FakeGoogleDriveBackupPort : GoogleDriveBackupPort {
        var manualBackupCalled = false
        var currentConfig = BackupConfig(backupAppFolder = "testFolder")

        override suspend fun performManualBackup(databasePath: String, imagePaths: List<String>): Boolean {
            manualBackupCalled = true
            return true
        }

        override suspend fun configureAutomaticBackup(config: BackupConfig): Boolean {
            currentConfig = config
            return true
        }

        override suspend fun getBackupConfig(): BackupConfig = currentConfig

        override suspend fun performBackup(databasePath: String, imagePaths: List<String>): Boolean {
            return performManualBackup(databasePath, imagePaths)
        }
    }
}
