package co.japl.android.ev_ride_connect.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import co.com.japl.ui.components.ModelConfigCard
import co.japl.android.ev_ride_connect.R
import co.japl.android.ev_ride_connect.controller.AVAILABLE_LLM_MODELS
import co.japl.android.ev_ride_connect.controller.LlmConfigViewModel
import co.japl.android.ev_ride_connect.navigation.AppNavigator

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
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LlmConfigFormCard(
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

        val activeCount = configs.count { it.isActive }
        val fallbackCount = (configs.size - activeCount).coerceAtLeast(0)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.llm_registered_models),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.llm_models_summary_badge, activeCount, fallbackCount),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        if (configs.isEmpty()) {
            Text(
                text = stringResource(R.string.llm_empty_configurations),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                configs.forEach { config ->
                    val createdText = if (config.createdAt.isNotBlank()) {
                        stringResource(R.string.llm_created_at, config.createdAt)
                    } else ""

                    var showDeleteConfirm by remember { mutableStateOf(false) }

                    ModelConfigCard(
                        providerName = config.modelName,
                        modelVersion = config.selectedVersion,
                        createdAtText = createdText,
                        isActive = config.isActive,
                        isSecondaryFallback = !config.isActive,
                        latencyText = if (config.isActive) stringResource(R.string.llm_latency_label, 142) else null,
                        temperatureText = if (config.isActive) stringResource(R.string.llm_temp_label, "0.3") else null,
                        onToggleActive = { isActive ->
                            viewModel.toggleActiveStatus(config.id, isActive)
                        },
                        onEdit = { viewModel.onEditConfig(config) },
                        onDuplicate = { viewModel.onDuplicateConfig(config) },
                        onDelete = { showDeleteConfirm = true }
                    )

                    if (showDeleteConfirm) {
                        AlertDialog(
                            onDismissRequest = { showDeleteConfirm = false },
                            title = { Text(stringResource(R.string.llm_delete_title)) },
                            text = { Text(stringResource(R.string.llm_delete_confirm_message)) },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        viewModel.deleteConfig(config.id)
                                        showDeleteConfirm = false
                                    },
                                    colors = ButtonDefaults.buttonColors(
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

                OutlinedButton(
                    onClick = { viewModel.onCancelEdit() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = stringResource(R.string.llm_add_new_model_button),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun LlmConfigFormCard(
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
    var passwordVisible by remember { mutableStateOf(false) }
    var expandedModel by remember { mutableStateOf(false) }
    var expandedVersion by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (editingConfigId != 0L) {
                            stringResource(R.string.llm_edit_title, editingConfigId)
                        } else {
                            stringResource(R.string.llm_api_key_card_title)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.llm_api_key_card_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedModel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.llm_model_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.clickable { expandedModel = !expandedModel }
                        )
                    },
                    shape = RoundedCornerShape(12.dp)
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

            Text(
                text = stringResource(R.string.llm_token_key_label),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = apiKeyInput,
                onValueChange = onApiKeyChanged,
                placeholder = { Text(stringResource(R.string.llm_secret_key_placeholder)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                isError = errorMessage != null,
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )

            if (availableVersions.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.llm_version_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
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
                        },
                        shape = RoundedCornerShape(12.dp)
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onFetchVersions,
                    enabled = apiKeyInput.isNotBlank() && !isFetchingVersions,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isFetchingVersions) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = 6.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = 4.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.llm_fetch_versions_button),
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                Button(
                    onClick = onValidate,
                    enabled = apiKeyInput.isNotBlank() && !isValidating,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    if (isValidating) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = 6.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = 4.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.llm_validate_button),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (editingConfigId != 0L) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onCancelEdit) {
                        Text(stringResource(R.string.llm_cancel_edit))
                    }
                    Button(
                        onClick = onSave,
                        enabled = apiKeyInput.isNotBlank() && !isValidating
                    ) {
                        Text(stringResource(R.string.llm_save_button))
                    }
                }
            } else {
                Button(
                    onClick = onSave,
                    enabled = apiKeyInput.isNotBlank() && !isValidating,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.llm_save_button),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            AnimatedVisibility(visible = errorMessage != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.llm_validation_error),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            AnimatedVisibility(visible = validationSuccessMessage != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(R.string.llm_connected_feedback),
                            color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
