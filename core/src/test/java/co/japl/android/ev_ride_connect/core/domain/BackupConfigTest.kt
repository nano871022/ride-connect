package co.japl.android.ev_ride_connect.core.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import uk.co.jemos.podam.api.PodamFactoryImpl

class BackupConfigTest {

    private val podamFactory = PodamFactoryImpl()

    @Test
    fun shouldInstantiateBackupConfigWithPodam() {
        val config = podamFactory.manufacturePojo(BackupConfig::class.java)

        assertThat(config).isNotNull
        assertThat(config.backupIntervalHours).isNotNull
        assertThat(config.lastBackupTimestamp).isNotNull
        assertThat(config.backupAppFolder).isNotNull
    }

    @Test
    fun shouldCreateBackupConfigWithGivenValues() {
        val config = BackupConfig(
            isAutoBackupEnabled = true,
            backupIntervalHours = 24,
            lastBackupTimestamp = 1620000000L,
            backupAppFolder = "appDataFolder"
        )

        assertThat(config.isAutoBackupEnabled).isTrue()
        assertThat(config.backupIntervalHours).isEqualTo(24)
        assertThat(config.lastBackupTimestamp).isEqualTo(1620000000L)
        assertThat(config.backupAppFolder).isEqualTo("appDataFolder")
    }
}
