package co.japl.android.ev_ride_connect.app.controller

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.japl.android.ev_ride_connect.core.domain.BackupConfig
import co.japl.android.ev_ride_connect.core.ports.GoogleDriveBackupPort
import co.japl.android.ev_ride_connect.database.GoogleDriveBackupSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val googleDriveBackupPort: GoogleDriveBackupPort,
    @ApplicationContext private val context: Context? = null
) : ViewModel() {

    private val _backupConfig = MutableStateFlow(BackupConfig())
    val backupConfig: StateFlow<BackupConfig> = _backupConfig.asStateFlow()

    private val _isBackingUp = MutableStateFlow(false)
    val isBackingUp: StateFlow<Boolean> = _isBackingUp.asStateFlow()

    private val _backupStatus = MutableStateFlow<String?>(null)
    val backupStatus: StateFlow<String?> = _backupStatus.asStateFlow()

    init {
        loadBackupConfig()
    }

    fun loadBackupConfig() {
        viewModelScope.launch {
            val config = googleDriveBackupPort.getBackupConfig()
            _backupConfig.value = config
        }
    }

    fun performManualBackup(databasePath: String = "", imagePaths: List<String> = emptyList()) {
        val targetPath = if (databasePath.isBlank()) {
            context?.getDatabasePath("app_database.db")?.absolutePath ?: "app_database.db"
        } else {
            databasePath
        }
        viewModelScope.launch {
            _isBackingUp.value = true
            _backupStatus.value = null
            val success = googleDriveBackupPort.performManualBackup(targetPath, imagePaths)
            _isBackingUp.value = false
            _backupStatus.value = if (success) {
                GoogleDriveBackupSettings.STATUS_SUCCESS
            } else {
                GoogleDriveBackupSettings.STATUS_FAILURE
            }
            if (success) {
                loadBackupConfig()
            }
        }
    }

    fun configureAutoBackup(enabled: Boolean, intervalHours: Int) {
        viewModelScope.launch {
            val updatedConfig = _backupConfig.value.copy(
                isAutoBackupEnabled = enabled,
                backupIntervalHours = intervalHours
            )
            val success = googleDriveBackupPort.configureAutomaticBackup(updatedConfig)
            if (success) {
                _backupConfig.value = updatedConfig
            }
        }
    }
}
