package co.japl.android.ev_ride_connect.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import co.japl.android.ev_ride_connect.R
import co.japl.android.ev_ride_connect.controller.AVAILABLE_LLM_MODELS
import co.japl.android.ev_ride_connect.controller.LlmConfigViewModel
import co.japl.android.ev_ride_connect.navigation.AppNavigator
import co.japl.android.ev_ride_connect.core.domain.LlmConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LlmConfigScreen(
    viewModel: LlmConfigViewModel,
    navigator: AppNavigator? = null,
    modifier: Modifier = Modifier
) {
    val configs by viewModel.configs.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val apiKeyInput by viewModel.apiKeyInput.collectAsState()
    val availableVersions by viewModel.availableVersions.collectAsState()
    val selectedVersion by viewModel.selectedVersion.collectAsState()
    val isFetchingVersions by viewModel.isFetchingVersions.collectAsState()
    val isValidating by viewModel.isValidating.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val editingConfigId by viewModel.editingConfigId.collectAsState()
    val validationSuccessMessage by viewModel.validationSuccessMessage.collectAsState()
    var leftMenuExpanded by remember { mutableStateOf(false) }
    var settingsMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.llm_config_title)) },
                navigationIcon = {
                    Box {
                        IconButton(onClick = { leftMenuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Navigation Menu"
                            )
                        }
                        DropdownMenu(
                            expanded = leftMenuExpanded,
                            onDismissRequest = { leftMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.dashboard_title)) },
                                onClick = {
                                    leftMenuExpanded = false
                                    navigator?.navigateToDashboard()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.nav_trip)) },
                                onClick = {
                                    leftMenuExpanded = false
                                    navigator?.navigateToTrip()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.nav_ev_data)) },
                                onClick = {
                                    leftMenuExpanded = false
                                    navigator?.navigateToEvData()
                                }
                            )
                        }
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { settingsMenuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings Menu"
                            )
                        }
                        DropdownMenu(
                            expanded = settingsMenuExpanded,
                            onDismissRequest = { settingsMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.llm_config_title)) },
                                onClick = {
                                    settingsMenuExpanded = false
                                    navigator?.navigateToLlmConfig()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.ev_config_title)) },
                                onClick = {
                                    settingsMenuExpanded = false
                                    navigator?.navigateToEvConfig()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.backup_title)) },
                                onClick = {
                                    settingsMenuExpanded = false
                                    navigator?.navigateToBackup()
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LlmConfigForm(
                selectedModel = selectedModel,
                apiKeyInput = apiKeyInput,
                availableVersions = availableVersions,
                selectedVersion = selectedVersion,
                isFetchingVersions = isFetchingVersions,
                isValidating = isValidating,
                errorMessage = errorMessage,
                editingConfigId = editingConfigId,
                validationSuccessMessage = validationSuccessMessage,
                onModelSelected = { viewModel.onModelSelected(it) },
                onApiKeyChanged = { viewModel.onApiKeyChanged(it) },
                onFetchVersions = { viewModel.fetchAvailableVersions() },
                onVersionSelected = { viewModel.onVersionSelected(it) },
                onValidate = { viewModel.validateApiKeyAndModel() },
                onCancelEdit = { viewModel.onCancelEdit() },
                onSave = { viewModel.saveConfig() }
            )

            Text(
                text = stringResource(R.string.llm_configurations_header),
                style = MaterialTheme.typography.titleLarge
            )

            if (configs.isEmpty()) {
                Text(
                    text = stringResource(R.string.llm_empty_configurations),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(configs, key = { it.id }) { config ->
                        LlmConfigItemRow(
                            config = config,
                            onToggleActive = { isActive ->
                                viewModel.toggleActiveStatus(config.id, isActive)
                            },
                            onEdit = { viewModel.onEditConfig(config) },
                            onDuplicate = { viewModel.onDuplicateConfig(config) },
                            onDelete = { viewModel.deleteConfig(config.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LlmConfigForm(
    selectedModel: String,
    apiKeyInput: String,
    availableVersions: List<String>,
    selectedVersion: String,
    isFetchingVersions: Boolean,
    isValidating: Boolean,
    errorMessage: String?,
    editingConfigId: Long,
    validationSuccessMessage: String?,
    onModelSelected: (String) -> Unit,
    onApiKeyChanged: (String) -> Unit,
    onFetchVersions: () -> Unit,
    onVersionSelected: (String) -> Unit,
    onValidate: () -> Unit,
    onCancelEdit: () -> Unit,
    onSave: () -> Unit
) {
    var expandedModel by remember { mutableStateOf(false) }
    var expandedVersion by remember { mutableStateOf(false) }

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
                Text(
                    text = if (editingConfigId != 0L) {
                        stringResource(R.string.llm_edit_title, editingConfigId)
                    } else {
                        stringResource(R.string.llm_model_label)
                    },
                    style = MaterialTheme.typography.titleMedium
                )

                if (editingConfigId != 0L) {
                    TextButton(onClick = onCancelEdit) {
                        Text(stringResource(R.string.llm_cancel_edit))
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedModel,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.clickable { expandedModel = !expandedModel }
                        )
                    }
                )

                DropdownMenu(
                    expanded = expandedModel,
                    onDismissRequest = { expandedModel = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AVAILABLE_LLM_MODELS.forEach { modelName ->
                        DropdownMenuItem(
                            text = { Text(modelName) },
                            onClick = {
                                onModelSelected(modelName)
                                expandedModel = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = apiKeyInput,
                onValueChange = onApiKeyChanged,
                label = { Text(stringResource(R.string.llm_api_key_label)) },
                placeholder = { Text(stringResource(R.string.llm_api_key_placeholder)) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                isError = errorMessage != null,
                singleLine = true
            )

            if (errorMessage != null) {
                Text(
                    text = stringResource(R.string.llm_validation_error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (validationSuccessMessage != null) {
                Text(
                    text = stringResource(R.string.llm_validation_success),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (availableVersions.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.llm_version_label),
                    style = MaterialTheme.typography.titleMedium
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedVersion,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.clickable { expandedVersion = !expandedVersion }
                            )
                        }
                    )

                    DropdownMenu(
                        expanded = expandedVersion,
                        onDismissRequest = { expandedVersion = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        availableVersions.forEach { version ->
                            DropdownMenuItem(
                                text = { Text(version) },
                                onClick = {
                                    onVersionSelected(version)
                                    expandedVersion = false
                                }
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onFetchVersions,
                        enabled = apiKeyInput.isNotBlank() && !isFetchingVersions
                    ) {
                        if (isFetchingVersions) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                        Text(stringResource(R.string.llm_fetch_versions_button))
                    }

                    OutlinedButton(
                        onClick = onValidate,
                        enabled = apiKeyInput.isNotBlank() && !isValidating
                    ) {
                        Text(stringResource(R.string.llm_validate_button))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isValidating) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }

                    Button(
                        onClick = onSave,
                        enabled = apiKeyInput.isNotBlank() && !isValidating
                    ) {
                        Text(stringResource(R.string.llm_save_button))
                    }
                }
            }
        }
    }
}

@Composable
private fun LlmConfigItemRow(
    config: LlmConfig,
    onToggleActive: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (config.selectedVersion.isNotBlank()) "${config.modelName} (${config.selectedVersion})" else config.modelName,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.llm_api_key_hidden),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (config.createdAt.isNotBlank()) {
                        Text(
                            text = stringResource(R.string.llm_created_at, config.createdAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (config.updatedAt.isNotBlank()) {
                        Text(
                            text = stringResource(R.string.llm_updated_at, config.updatedAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (config.isActive) {
                            stringResource(R.string.llm_status_active)
                        } else {
                            stringResource(R.string.llm_status_inactive)
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                    Switch(
                        checked = config.isActive,
                        onCheckedChange = onToggleActive
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit"
                    )
                }
                IconButton(onClick = onDuplicate) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Duplicate"
                    )
                }
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.llm_delete_title)) },
            text = { Text(stringResource(R.string.llm_delete_confirm_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.llm_delete_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.cancel_button))
                }
            }
        )
    }
}
