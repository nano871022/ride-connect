package co.japl.android.ev_ride_connect.app.ui

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
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import co.japl.android.ev_ride_connect.app.R
import co.japl.android.ev_ride_connect.app.controller.AVAILABLE_LLM_MODELS
import co.japl.android.ev_ride_connect.app.controller.LlmConfigViewModel
import co.japl.android.ev_ride_connect.app.navigation.AppNavigator
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
    val isValidating by viewModel.isValidating.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.llm_config_title)) },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LlmConfigForm(
                selectedModel = selectedModel,
                apiKeyInput = apiKeyInput,
                isValidating = isValidating,
                errorMessage = errorMessage,
                onModelSelected = { viewModel.onModelSelected(it) },
                onApiKeyChanged = { viewModel.onApiKeyChanged(it) },
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
                            }
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
    isValidating: Boolean,
    errorMessage: String?,
    onModelSelected: (String) -> Unit,
    onApiKeyChanged: (String) -> Unit,
    onSave: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

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
            Text(
                text = stringResource(R.string.llm_model_label),
                style = MaterialTheme.typography.titleMedium
            )

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
                            modifier = Modifier.clickable { expanded = !expanded }
                        )
                    }
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AVAILABLE_LLM_MODELS.forEach { modelName ->
                        DropdownMenuItem(
                            text = { Text(modelName) },
                            onClick = {
                                onModelSelected(modelName)
                                expanded = false
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

            Row(
                modifier = Modifier.align(Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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

@Composable
private fun LlmConfigItemRow(
    config: LlmConfig,
    onToggleActive: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = config.modelName,
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
    }
}
