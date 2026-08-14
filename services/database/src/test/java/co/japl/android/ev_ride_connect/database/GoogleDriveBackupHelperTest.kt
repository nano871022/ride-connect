package co.japl.android.ev_ride_connect.database

import co.japl.android.ev_ride_connect.core.domain.BackupConfig
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import uk.co.jemos.podam.api.PodamFactoryImpl

class GoogleDriveBackupHelperTest {

    private lateinit var backupHelper: GoogleDriveBackupHelper
    private val podamFactory = PodamFactoryImpl()

    @Before
    fun setUp() {
        backupHelper = GoogleDriveBackupHelper()
    }

    @Test
    fun shouldPerformManualBackupSuccessfully() = runTest {
        val databasePath = "/data/data/co.japl.android.ev_ride_connect/databases/app.db"
        val imagePaths = listOf("/data/data/co.japl.android.ev_ride_connect/files/scooter.png")

        val result = backupHelper.performManualBackup(databasePath, imagePaths)

        assertThat(result).isTrue()
    }

    @Test
    fun shouldConfigureAutomaticBackup() = runTest {
        val config = podamFactory.manufacturePojo(BackupConfig::class.java).copy(
            isAutoBackupEnabled = true,
            backupIntervalHours = 12
        )

        val result = backupHelper.configureAutomaticBackup(config)

        assertThat(result).isTrue()
        val currentConfig = backupHelper.getBackupConfig()
        assertThat(currentConfig.isAutoBackupEnabled).isTrue()
        assertThat(currentConfig.backupIntervalHours).isEqualTo(12)
    }

    @Test
    fun shouldGetBackupConfigWithDefaults() = runTest {
        val config = backupHelper.getBackupConfig()

        assertThat(config).isNotNull
        assertThat(config.backupAppFolder).isEqualTo(GoogleDriveBackupSettings.DRIVE_APP_FOLDER)
    }

    @Test
    fun shouldPerformBackupWithDatabaseAndImages() = runTest {
        val databasePath = "/path/to/database.db"
        val imagePaths = listOf("/path/to/img1.jpg", "/path/to/img2.jpg")

        val result = backupHelper.performBackup(databasePath, imagePaths)

        assertThat(result).isTrue()
    }
}
