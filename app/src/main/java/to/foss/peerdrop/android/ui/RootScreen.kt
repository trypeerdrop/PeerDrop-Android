package to.foss.peerdrop.android.ui

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import to.foss.peerdrop.android.ui.devices.DevicesScreen
import to.foss.peerdrop.android.ui.received.ReceivedScreen
import to.foss.peerdrop.android.ui.settings.SettingsScreen
import to.foss.peerdrop.android.ui.transfers.TransfersScreen
import to.foss.peerdrop.android.ui.transfers.TransfersViewModel

@Composable
fun RootScreen(
    sharedUris:  List<Uri>,
    onPickFiles: ((List<Uri>) -> Unit) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val transfersViewModel: TransfersViewModel = koinViewModel()
    val transfers by transfersViewModel.transfers.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick  = { selectedTab = 0 },
                    icon     = { Icon(Icons.Default.People, contentDescription = null) },
                    label    = { Text("Devices") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick  = { selectedTab = 1 },
                    icon     = {
                        BadgedBox(
                            badge = {
                                if (transfers.isNotEmpty())
                                    Badge { Text("${transfers.size}") }
                            }
                        ) {
                            Icon(Icons.Default.SwapVert, contentDescription = null)
                        }
                    },
                    label = { Text("Transfers") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick  = { selectedTab = 2 },
                    icon     = { Icon(Icons.Default.Inbox, contentDescription = null) },
                    label    = { Text("Received") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick  = { selectedTab = 3 },
                    icon     = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label    = { Text("Settings") }
                )
            }
        }
    ) { padding ->
        when (selectedTab) {
            0 -> DevicesScreen(
                contentPadding = padding,
                sharedUris     = sharedUris,
                onPickFiles    = onPickFiles
            )
            1 -> TransfersScreen(contentPadding = padding)
            2 -> ReceivedScreen(contentPadding = padding)
            3 -> SettingsScreen(contentPadding = padding)
        }
    }
}