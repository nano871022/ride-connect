package co.japl.android.ev_ride_connect.controller

import co.japl.android.ev_ride_connect.core.domain.BackupConfig
import co.japl.android.ev_ride_connect.core.domain.BackupStatus
import co.japl.android.ev_ride_connect.core.ports.GoogleDriveBackupPort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BackupViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeBackupPort: FakeGoogleDriveBackupPort
    private lateinit var viewModel: BackupViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeBackupPort = FakeGoogleDriveBackupPort()
        viewModel = BackupViewModel(fakeBackupPort)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun shouldLoadBackupConfigOnInitialization() = runTest {
        testScheduler.runCurrent()

        val config = viewModel.backupConfig.value
        assertThat(config).isNotNull
        assertThat(config.backupAppFolder).isEqualTo("appDataFolder")
    }

    @Test
    fun shouldPerformManualBackupSuccessfully() = runTest {
        viewModel.performManualBackup("/path/to/db", listOf("/path/to/img.png"))
        testScheduler.runCurrent()

        assertThat(fakeBackupPort.manualBackupCalled).isTrue()
        assertThat(viewModel.isBackingUp.value).isFalse()
        assertThat(viewModel.backupStatus.value).isEqualTo(BackupStatus.SUCCESS)
    }

    @Test
    fun shouldUpdateAutoBackupConfiguration() = runTest {
        viewModel.configureAutoBackup(true, 12)
        testScheduler.runCurrent()

        val config = viewModel.backupConfig.value
        assertThat(config.isAutoBackupEnabled).isTrue()
        assertThat(config.backupIntervalHours).isEqualTo(12)
    }

    private class FakeGoogleDriveBackupPort : GoogleDriveBackupPort {
        var manualBackupCalled = false
        var currentConfig = BackupConfig(false, 24, 0L, "appDataFolder")

        override suspend fun performManualBackup(databasePath: String, imagePaths: List<String>): Boolean {
            manualBackupCalled = true
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
