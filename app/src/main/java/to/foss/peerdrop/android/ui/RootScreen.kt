package to.foss.peerdrop.android.ui.root

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import to.holepunch.peerdrop.android.ui.devices.DevicesScreen
import to.holepunch.peerdrop.android.ui.transfers.TransfersScreen
import to.holepunch.peerdrop.android.ui.received.ReceivedScreen
import to.holepunch.peerdrop.android.ui.settings.SettingsScreen
import org.koin.androidx.compose.koinViewModel
import to.holepunch.peerdrop.android.ui.transfers.TransfersViewModel

@Composable
fun RootScreen(
    sharedUris:  List<Uri>,
    onPickFiles: ((List<Uri>) -> Unit) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(if (sharedUris.isNotEmpty()) 0 else 0) }
    val transfersViewModel: TransfersViewModel = koinViewModel()
    val transfers by transfersViewModel.transfers.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick  = { selectedTab = 0 },
                    icon     = { Icon(Icons.Default.People, null) },
                    label    = { Text("Devices") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick  = { selectedTab = 1 },
                    icon     = { Icon(Icons.Default.SwapVert, null) },
                    label    = { Text("Transfers") },
                    badge    = {
                        if (transfers.isNotEmpty())
                            Badge { Text("${transfers.size}") }
                    }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick  = { selectedTab = 2 },
                    icon     = { Icon(Icons.Default.Inbox, null) },
                    label    = { Text("Received") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick  = { selectedTab = 3 },
                    icon     = { Icon(Icons.Default.Settings, null) },
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
