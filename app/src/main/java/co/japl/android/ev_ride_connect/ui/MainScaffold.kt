package co.japl.android.ev_ride_connect.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.japl.android.ev_ride_connect.R
import co.japl.android.ev_ride_connect.navigation.AppNavigator
import co.japl.android.ev_ride_connect.navigation.AppScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    navigator: AppNavigator,
    currentScreen: AppScreen,
    isTracking: Boolean = false,
    title: @Composable () -> Unit = { DefaultTitle(currentScreen, isTracking) },
    topBarActions: @Composable RowScope.() -> Unit = {},
    containerColor: Color? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val defaultContainerColor = when (currentScreen) {
        AppScreen.TRIP, AppScreen.BACKUP, AppScreen.TRIP_DETAIL -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceContainerLowest
    }

    val finalContainerColor = containerColor ?: defaultContainerColor

    val onContainerColor = if (finalContainerColor == MaterialTheme.colorScheme.primaryContainer) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentScreen != AppScreen.TRIP_DETAIL,
        drawerContent = {
            AppDrawerContent(
                navigator = navigator,
                currentScreen = currentScreen,
                onCloseDrawer = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = title,
                    navigationIcon = {
                        if (currentScreen == AppScreen.TRIP_DETAIL) {
                            IconButton(onClick = { navigator.navigateToTrip() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.trip_back_button)
                                )
                            }
                        } else {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Open Navigation Menu"
                                )
                            }
                        }
                    },
                    actions = {
                        topBarActions()
                        SettingsMenu(navigator)
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = finalContainerColor,
                        titleContentColor = onContainerColor,
                        navigationIconContentColor = onContainerColor,
                        actionIconContentColor = onContainerColor
                    )
                )
            }
        ) { innerPadding ->
            content(innerPadding)
        }
    }
}

@Composable
private fun AppDrawerContent(
    navigator: AppNavigator,
    currentScreen: AppScreen,
    onCloseDrawer: () -> Unit
) {
    ModalDrawerSheet {
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.app_name),
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
            label = { Text(stringResource(R.string.dashboard_title)) },
            selected = currentScreen == AppScreen.DASHBOARD,
            onClick = {
                onCloseDrawer()
                navigator.navigateToDashboard()
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Route, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_trip)) },
            selected = currentScreen == AppScreen.TRIP,
            onClick = {
                onCloseDrawer()
                navigator.navigateToTrip()
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.History, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_ev_data)) },
            selected = currentScreen == AppScreen.EV_DATA,
            onClick = {
                onCloseDrawer()
                navigator.navigateToEvData()
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
    }
}

@Composable
private fun SettingsMenu(navigator: AppNavigator) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings Menu"
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.llm_config_title)) },
                onClick = {
                    expanded = false
                    navigator.navigateToLlmConfig()
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.ev_config_title)) },
                onClick = {
                    expanded = false
                    navigator.navigateToEvConfig()
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.backup_title)) },
                onClick = {
                    expanded = false
                    navigator.navigateToBackup()
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.about)) },
                onClick = {
                    expanded = false
                    navigator.navigateToAbout()
                }
            )
        }
    }
}

@Composable
private fun DefaultTitle(currentScreen: AppScreen, isTracking: Boolean = false) {
    val trackingText = stringResource(R.string.scaffold_tracking_title)
    val screenTitle = if (isTracking) trackingText else getTitleName(currentScreen)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if(!screenTitle.isBlank()) {
                Text(
                    text = screenTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Thin
                )
            }
        }
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outline)
                )
                Text(
                    text = stringResource(R.string.dashboard_no_vehicle_connection),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun getTitleName(currentScreen: AppScreen): String =
    when (currentScreen) {
        AppScreen.EV_CONFIG -> stringResource(R.string.ev_config_title)
        AppScreen.EV_DATA -> stringResource(R.string.history_title)
        AppScreen.LLM_CONFIG -> stringResource(R.string.llm_config_title)
        AppScreen.BACKUP -> stringResource(R.string.backup_title)
        AppScreen.TRIP -> stringResource(R.string.trip_title)
        AppScreen.TRIP_DETAIL -> stringResource(R.string.trip_detail_title)
        AppScreen.ABOUT -> stringResource(R.string.about)
        else -> ""
    }

