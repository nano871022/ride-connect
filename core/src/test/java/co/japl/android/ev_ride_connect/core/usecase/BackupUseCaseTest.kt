package co.japl.android.ev_ride_connect.core.usecase

import co.japl.android.ev_ride_connect.interfaces.model.BackupConfig
import co.japl.android.ev_ride_connect.interfaces.ports.GoogleDriveBackupPort
import co.japl.android.ev_ride_connect.interfaces.usecase.BackupUseCase
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
            googleDriveBackupPort = fakeBackupPort,
            getBackupConfigUseCase = GetBackupConfigUseCase(fakeBackupPort),
            performManualBackupUseCase = PerformManualBackupUseCase(fakeBackupPort),
            configureAutoBackupUseCase = ConfigureAutoBackupUseCase(fakeBackupPort)
        )
    }

    @Test
    fun shouldGetBackupConfig() = runTest {
        val config = useCase.getBackupConfig()

        assertThat(config.backupAppFolder).isEqualTo("testFolder")
    }

    @Test
    fun shouldPerformManualBackup() = runTest {
        val result = useCase.performManualBackup("dbPath", listOf("img1"))

        assertThat(result).isTrue()
    }

    @Test
    fun shouldConfigureAutoBackup() = runTest {
        val newConfig = BackupConfig(isAutoBackupEnabled = true, backupIntervalHours = 12)

        val result = useCase.configureAutoBackup(newConfig)

        assertThat(result).isTrue()
        assertThat(fakeBackupPort.currentConfig.isAutoBackupEnabled).isTrue()
    }

    private class FakeGoogleDriveBackupPort : GoogleDriveBackupPort {
        var currentConfig = BackupConfig(backupAppFolder = "testFolder")

        override suspend fun performManualBackup(databasePath: String, imagePaths: List<String>): Boolean = true

        override suspend fun performBackup(databasePath: String, imagePaths: List<String>): Boolean = true

        override suspend fun configureAutomaticBackup(config: BackupConfig): Boolean {
            currentConfig = config
            return true
        }

        override suspend fun getBackupConfig(): BackupConfig = currentConfig
    }
}
