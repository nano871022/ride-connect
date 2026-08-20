package co.japl.android.ev_ride_connect.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.com.japl.ui.components.SegmentOption
import co.com.japl.ui.components.SegmentedChipGroup
import co.com.japl.ui.components.SettingSwitchRow
import co.com.japl.ui.components.StatusCard
import co.japl.android.ev_ride_connect.app.R
import co.japl.android.ev_ride_connect.app.controller.BackupViewModel
import co.japl.android.ev_ride_connect.app.navigation.AppNavigator
import co.japl.android.ev_ride_connect.core.domain.BackupConfig
import co.japl.android.ev_ride_connect.core.domain.BackupStatus
import co.japl.android.ev_ride_connect.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    viewModel: BackupViewModel,
    databasePath: String = "",
    imagePaths: List<String> = emptyList(),
    navigator: AppNavigator? = null,
    modifier: Modifier = Modifier
) {
    val backupConfig by viewModel.backupConfig.collectAsState()
    val isBackingUp by viewModel.isBackingUp.collectAsState()
    val backupStatus by viewModel.backupStatus.collectAsState()
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.backup_title)) },
                navigationIcon = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Settings Menu"
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.dashboard_title)) },
                                onClick = {
                                    menuExpanded = false
                                    navigator?.navigateToDashboard()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.ev_config_title)) },
                                onClick = {
                                    menuExpanded = false
                                    navigator?.navigateToEvConfig()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.llm_config_title)) },
                                onClick = {
                                    menuExpanded = false
                                    navigator?.navigateToLlmConfig()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.backup_title)) },
                                onClick = {
                                    menuExpanded = false
                                    navigator?.navigateToBackup()
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
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
            val lastBackupText = if (backupConfig.lastBackupTimestamp > 0) {
                stringResource(R.string.last_backup_time, DateUtils.formatTimestamp(backupConfig.lastBackupTimestamp))
            } else null

            val statusMsg = when (backupStatus) {
                BackupStatus.SUCCESS -> stringResource(R.string.backup_success)
                BackupStatus.FAILURE -> stringResource(R.string.backup_failure)
                else -> null
            }

            StatusCard(
                title = stringResource(R.string.backup_title),
                isLoading = isBackingUp,
                lastUpdatedText = lastBackupText,
                actionButtonText = stringResource(R.string.backup_now),
                onActionClick = { viewModel.performManualBackup(databasePath, imagePaths) },
                statusMessage = statusMsg,
                isSuccessStatus = backupStatus == BackupStatus.SUCCESS
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
            SettingSwitchRow(
                title = stringResource(R.string.auto_backup_title),
                subtitle = stringResource(R.string.auto_backup_subtitle),
                checked = backupConfig.isAutoBackupEnabled,
                onCheckedChange = onToggleAutoBackup
            )

            if (backupConfig.isAutoBackupEnabled) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.backup_interval_title),
                    style = MaterialTheme.typography.labelLarge
                )
                val intervalOptions = listOf(6, 12, 24, 48).map { hours ->
                    SegmentOption(hours, stringResource(R.string.backup_interval_hours, hours))
                }
                SegmentedChipGroup(
                    options = intervalOptions,
                    selectedOption = backupConfig.backupIntervalHours,
                    onOptionSelected = onIntervalSelected
                )
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
