package co.japl.android.ev_ride_connect.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.japl.android.ev_ride_connect.app.R
import co.japl.android.ev_ride_connect.app.controller.BackupViewModel
import co.japl.android.ev_ride_connect.core.domain.BackupConfig
import co.japl.android.ev_ride_connect.database.GoogleDriveBackupSettings
import co.japl.android.ev_ride_connect.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    viewModel: BackupViewModel,
    databasePath: String = "",
    imagePaths: List<String> = emptyList(),
    modifier: Modifier = Modifier
) {
    val backupConfig by viewModel.backupConfig.collectAsState()
    val isBackingUp by viewModel.isBackingUp.collectAsState()
    val backupStatus by viewModel.backupStatus.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.backup_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ManualBackupCard(
                isBackingUp = isBackingUp,
                backupStatus = backupStatus,
                lastBackupTimestamp = backupConfig.lastBackupTimestamp,
                onBackupNow = { viewModel.performManualBackup(databasePath, imagePaths) }
            )

            AutoBackupCard(
                backupConfig = backupConfig,
                onToggleAutoBackup = { enabled ->
                    viewModel.configureAutoBackup(enabled, backupConfig.backupIntervalHours)
                },
                onIntervalSelected = { interval ->
                    viewModel.configureAutoBackup(backupConfig.isAutoBackupEnabled, interval)
                }
            )

            BackupInfoCard(backupConfig = backupConfig)
        }
    }
}

@Composable
private fun ManualBackupCard(
    isBackingUp: Boolean,
    backupStatus: String?,
    lastBackupTimestamp: Long,
    onBackupNow: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.backup_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (lastBackupTimestamp > 0) {
                val formattedDate = DateUtils.formatTimestamp(lastBackupTimestamp)
                Text(
                    text = stringResource(R.string.last_backup_time, formattedDate),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (isBackingUp) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator()
                    Text(text = stringResource(R.string.backup_in_progress))
                }
            } else {
                Button(
                    onClick = onBackupNow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.backup_now))
                }
            }

            backupStatus?.let { status ->
                val isSuccess = status == GoogleDriveBackupSettings.STATUS_SUCCESS
                val message = if (isSuccess) {
                    stringResource(R.string.backup_success)
                } else {
                    stringResource(R.string.backup_failure)
                }
                val color = if (isSuccess) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
                Text(
                    text = message,
                    color = color,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AutoBackupCard(
    backupConfig: BackupConfig,
    onToggleAutoBackup: (Boolean) -> Unit,
    onIntervalSelected: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.auto_backup_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.auto_backup_subtitle),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Switch(
                    checked = backupConfig.isAutoBackupEnabled,
                    onCheckedChange = onToggleAutoBackup
                )
            }

            if (backupConfig.isAutoBackupEnabled) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.backup_interval_title),
                    style = MaterialTheme.typography.labelLarge
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val intervals = listOf(6, 12, 24, 48)
                    intervals.forEach { hours ->
                        FilterChip(
                            selected = backupConfig.backupIntervalHours == hours,
                            onClick = { onIntervalSelected(hours) },
                            label = { Text(stringResource(R.string.backup_interval_hours, hours)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BackupInfoCard(backupConfig: BackupConfig) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.backup_app_folder),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = backupConfig.backupAppFolder,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
