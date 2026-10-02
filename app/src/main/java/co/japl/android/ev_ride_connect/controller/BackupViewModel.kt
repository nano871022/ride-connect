package co.japl.android.ev_ride_connect.controller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.japl.android.ev_ride_connect.interfaces.model.BackupConfig
import co.japl.android.ev_ride_connect.interfaces.model.BackupStatus
import co.japl.android.ev_ride_connect.interfaces.usecase.BackupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupUseCase: BackupUseCase
) : ViewModel() {

    private val _backupConfig = MutableStateFlow(BackupConfig())
    val backupConfig: StateFlow<BackupConfig> = _backupConfig.asStateFlow()

    private val _isBackingUp = MutableStateFlow(false)
    val isBackingUp: StateFlow<Boolean> = _isBackingUp.asStateFlow()

    private val _backupStatus = MutableStateFlow(BackupStatus.IDLE)
    val backupStatus: StateFlow<BackupStatus> = _backupStatus.asStateFlow()

    init {
        loadBackupConfig()
    }

    fun loadBackupConfig() {
        viewModelScope.launch {
            val config = backupUseCase.getBackupConfig()
            _backupConfig.value = config
        }
    }

    fun performManualBackup(databasePath: String = "", imagePaths: List<String> = emptyList()) {
        viewModelScope.launch {
            _isBackingUp.value = true
            _backupStatus.value = BackupStatus.IN_PROGRESS
            val success = backupUseCase.performManualBackup(databasePath, imagePaths)
            _isBackingUp.value = false
            _backupStatus.value = if (success) BackupStatus.SUCCESS else BackupStatus.FAILURE
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
            val success = backupUseCase.configureAutoBackup(updatedConfig)
            if (success) {
                _backupConfig.value = updatedConfig
            }
        }
    }
}
