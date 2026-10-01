package co.japl.android.ev_ride_connect.controller

import co.japl.android.ev_ride_connect.interfaces.model.BackupConfig
import co.japl.android.ev_ride_connect.interfaces.model.BackupStatus
import co.japl.android.ev_ride_connect.interfaces.ports.GoogleDriveBackupPort
import co.japl.android.ev_ride_connect.core.usecase.BackupUseCaseImpl
import co.japl.android.ev_ride_connect.core.usecase.ConfigureAutoBackupUseCase
import co.japl.android.ev_ride_connect.core.usecase.GetBackupConfigUseCase
import co.japl.android.ev_ride_connect.core.usecase.PerformManualBackupUseCase
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

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeBackupPort = FakeGoogleDriveBackupPort()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun shouldLoadBackupConfigOnInitialization() = runTest {
        val viewModel = BackupViewModel(
            BackupUseCaseImpl(
                googleDriveBackupPort = fakeBackupPort,
                getBackupConfigUseCase = GetBackupConfigUseCase(fakeBackupPort),
                performManualBackupUseCase = PerformManualBackupUseCase(fakeBackupPort),
                configureAutoBackupUseCase = ConfigureAutoBackupUseCase(fakeBackupPort)
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val config = viewModel.backupConfig.value
        assertThat(config.backupAppFolder).isEqualTo("appDataFolder")
    }

    private class FakeGoogleDriveBackupPort : GoogleDriveBackupPort {
        var currentConfig = BackupConfig(false, 24, 0L, "appDataFolder")

        override suspend fun performManualBackup(databasePath: String, imagePaths: List<String>): Boolean = true

        override suspend fun performBackup(databasePath: String, imagePaths: List<String>): Boolean = true

        override suspend fun configureAutomaticBackup(config: BackupConfig): Boolean {
            currentConfig = config
            return true
        }

        override suspend fun getBackupConfig(): BackupConfig {
            return currentConfig
        }
    }
}
