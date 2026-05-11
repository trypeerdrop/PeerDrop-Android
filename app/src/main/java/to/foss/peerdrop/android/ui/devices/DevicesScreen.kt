package to.foss.peerdrop.android.ui.devices

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import to.holepunch.peerdrop.android.data.model.PeerDevice
import to.holepunch.peerdrop.android.ui.theme.PeerDropColors

@Composable
fun DevicesScreen(
    contentPadding: PaddingValues,
    sharedUris:     List<Uri>,
    onPickFiles:    ((List<Uri>) -> Unit) -> Unit,
    viewModel:      DevicesViewModel = koinViewModel()
) {
    val peers by viewModel.peers.collectAsStateWithLifecycle()
    val contacts  = peers.filter { !it.isOwnDevice }
    val myDevices = peers.filter {  it.isOwnDevice }

    var selectedPeer    by remember { mutableStateOf<PeerDevice?>(null) }
    var showAddDialog   by remember { mutableStateOf(false) }

    // Auto-navigate to send panel if launched via share intent
    LaunchedEffect(sharedUris) {
        if (sharedUris.isNotEmpty() && selectedPeer == null && contacts.isNotEmpty()) {
            selectedPeer = contacts.firstOrNull { it.isOnline } ?: contacts.firstOrNull()
        }
    }

    Box(Modifier.fillMaxSize().padding(contentPadding)) {
        if (selectedPeer != null) {
            SendPanel(
                peer        = selectedPeer!!,
                sharedUris  = sharedUris,
                onBack      = { selectedPeer = null },
                onPickFiles = { onPickFiles { uris -> uris.forEach { uri ->
                    viewModel.sendFile(uri.toString(), selectedPeer!!.discoveryKey)
                }}},
                onSendUris  = { uris -> uris.forEach { uri ->
                    viewModel.sendFile(uri.toString(), selectedPeer!!.discoveryKey)
                }},
                onForget    = {
                    viewModel.forgetPeer(selectedPeer!!.discoveryKey)
                    selectedPeer = null
                }
            )
        } else {
            PeerList(
                myDevices     = myDevices,
                contacts      = contacts,
                onSelectPeer  = { selectedPeer = it },
                onAddContact  = { showAddDialog = true }
            )
        }
    }

    if (showAddDialog) {
        AddContactDialog(
            onDismiss = { showAddDialog = false },
            onAdd     = { id -> viewModel.connectPeer(id); showAddDialog = false }
        )
    }
}

// ── Peer list ─────────────────────────────────────────────────────────────────

@Composable
private fun PeerList(
    myDevices:    List<PeerDevice>,
    contacts:     List<PeerDevice>,
    onSelectPeer: (PeerDevice) -> Unit,
    onAddContact: () -> Unit
) {
    LazyColumn(Modifier.fillMaxSize()) {
        item {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "PeerDrop",
                    fontSize     = 28.sp,
                    fontWeight   = FontWeight.Bold,
                    color        = PeerDropColors.OnBackground
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onAddContact) {
                    Icon(Icons.Default.PersonAdd, null, tint = PeerDropColors.Primary)
                }
            }
        }

        if (myDevices.isNotEmpty()) {
            item { SectionHeader("MY DEVICES") }
            items(myDevices, key = { it.discoveryKey }) { peer ->
                PeerRow(peer = peer, onClick = { onSelectPeer(peer) })
            }
        }

        item { SectionHeader("PEOPLE") }

        if (contacts.isEmpty()) {
            item { EmptyContacts() }
        } else {
            items(contacts, key = { it.discoveryKey }) { peer ->
                PeerRow(peer = peer, onClick = { onSelectPeer(peer) })
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        title,
        modifier      = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
        fontSize      = 11.sp,
        fontWeight    = FontWeight.SemiBold,
        letterSpacing = 1.sp,
        color         = PeerDropColors.Subtle
    )
}

@Composable
fun PeerRow(peer: PeerDevice, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (peer.isOnline) Color(0xFF1A1A3E) else Color(0xFF1A1A1A)),
            Alignment.Center
        ) {
            Text(
                text = when (peer.platform) {
                    "darwin"  -> "💻"
                    "ios"     -> "📱"
                    "linux"   -> "🖥"
                    "android" -> "📱"
                    else      -> "🖥"
                },
                fontSize = 20.sp
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text       = peer.displayName.ifEmpty { peer.discoveryKey.take(12) + "…" },
                color      = PeerDropColors.OnBackground,
                fontWeight = FontWeight.Medium,
                fontSize   = 15.sp
            )
            Text(
                text     = if (peer.isOnline) "Online" else "Offline",
                color    = if (peer.isOnline) PeerDropColors.Online else PeerDropColors.Subtle,
                fontSize = 12.sp
            )
        }

        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (peer.isOnline) PeerDropColors.Online else PeerDropColors.VerySubtle)
        )
    }
}

@Composable
private fun EmptyContacts() {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("👤", fontSize = 40.sp)
        Spacer(Modifier.height(8.dp))
        Text("No contacts yet", color = PeerDropColors.Subtle, fontSize = 14.sp)
        Text("Tap + to add someone", color = PeerDropColors.VerySubtle, fontSize = 12.sp)
    }
}

// ── Send panel ────────────────────────────────────────────────────────────────

@Composable
private fun SendPanel(
    peer:       PeerDevice,
    sharedUris: List<Uri>,
    onBack:     () -> Unit,
    onPickFiles:() -> Unit,
    onSendUris: (List<Uri>) -> Unit,
    onForget:   () -> Unit
) {
    // Auto-send if launched via share intent
    LaunchedEffect(peer, sharedUris) {
        if (sharedUris.isNotEmpty()) onSendUris(sharedUris)
    }

    Column(Modifier.fillMaxSize()) {
        // Header
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null,
                    tint = PeerDropColors.OnBackground)
            }
            Box(
                Modifier.size(40.dp).clip(CircleShape)
                    .background(if (peer.isOnline) Color(0xFF1A1A3E) else Color(0xFF1A1A1A)),
                Alignment.Center
            ) {
                Text(if (peer.isOnline) "🖥" else "💤", fontSize = 18.sp)
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    peer.displayName,
                    fontWeight = FontWeight.SemiBold,
                    color      = PeerDropColors.OnBackground
                )
                Text(
                    if (peer.isOnline) "Ready to receive" else "Offline",
                    color    = if (peer.isOnline) PeerDropColors.Online else PeerDropColors.Warning,
                    fontSize = 12.sp
                )
            }
            TextButton(onClick = onForget) {
                Text("Remove", color = PeerDropColors.Danger, fontSize = 12.sp)
            }
        }

        HorizontalDivider(color = PeerDropColors.SurfaceVar)

        // Drop zone
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0D18)),
            shape  = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, PeerDropColors.SurfaceVar)
        ) {
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("⬇", fontSize = 44.sp, color = PeerDropColors.VerySubtle)
                Spacer(Modifier.height(12.dp))
                Text(
                    "Tap to pick files to send",
                    color    = PeerDropColors.Subtle,
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(16.dp))
                OutlinedButton(
                    onClick = onPickFiles,
                    enabled = peer.isOnline,
                    border  = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (peer.isOnline) PeerDropColors.Primary else Color(0xFF222222)
                    )
                ) {
                    Text(
                        "Browse Files",
                        color = if (peer.isOnline) PeerDropColors.Primary else PeerDropColors.VerySubtle
                    )
                }
            }
        }
    }
}

// ── Add Contact dialog ────────────────────────────────────────────────────────

@Composable
private fun AddContactDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var peerID by remember { mutableStateOf("") }
    val valid  = peerID.length == 64

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = PeerDropColors.Surface,
        title = {
            Text("Add Contact", color = PeerDropColors.OnBackground, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    "Paste the 64-character Peer ID",
                    color    = PeerDropColors.Subtle,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value         = peerID,
                    onValueChange = { peerID = it.trim() },
                    placeholder   = {
                        Text("e.g. a1b2c3d4…", color = PeerDropColors.VerySubtle, fontSize = 12.sp)
                    },
                    singleLine    = true,
                    fontFamily    = FontFamily.Monospace,
                    isError       = peerID.isNotEmpty() && !valid,
                    supportingText = {
                        if (peerID.isNotEmpty() && !valid)
                            Text("${peerID.length}/64 characters", fontSize = 11.sp)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = PeerDropColors.Primary,
                        unfocusedBorderColor = PeerDropColors.Border,
                        focusedTextColor     = PeerDropColors.OnBackground,
                        unfocusedTextColor   = PeerDropColors.OnBackground,
                        errorBorderColor     = PeerDropColors.Warning,
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick  = { onAdd(peerID) },
                enabled  = valid,
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = PeerDropColors.Primary,
                    disabledContainerColor = PeerDropColors.VerySubtle
                )
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = PeerDropColors.Subtle)
            }
        }
    )
}
