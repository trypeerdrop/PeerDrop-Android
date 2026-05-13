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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import to.foss.peerdrop.android.data.model.PeerDevice
import to.foss.peerdrop.android.ui.peerDropColors

@Composable
fun DevicesScreen(
    contentPadding: PaddingValues,
    sharedUris:     List<Uri>,
    onPickFiles:    ((List<Uri>) -> Unit) -> Unit,
    viewModel:      DevicesViewModel = koinViewModel()
) {
    val context   = LocalContext.current
    val peers     by viewModel.peers.collectAsStateWithLifecycle()
    val contacts  = peers.filter { !it.isOwnDevice }
    val myDevices = peers.filter {  it.isOwnDevice }

    var selectedPeer  by remember { mutableStateOf<PeerDevice?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

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
                onPickFiles = {
                    onPickFiles { uris ->
                        viewModel.sendFiles(context, uris, selectedPeer!!.discoveryKey)
                    }
                },
                onSendUris  = { uris ->
                    viewModel.sendFiles(context, uris, selectedPeer!!.discoveryKey)
                },
                onForget    = {
                    viewModel.forgetPeer(selectedPeer!!.discoveryKey)
                    selectedPeer = null
                }
            )
        } else {
            PeerList(
                myDevices    = myDevices,
                contacts     = contacts,
                onSelectPeer = { selectedPeer = it },
                onAddContact = { showAddDialog = true }
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

// ── Peer list ──────────────────────────────────────────────────────────────────

@Composable
private fun PeerList(
    myDevices:    List<PeerDevice>,
    contacts:     List<PeerDevice>,
    onSelectPeer: (PeerDevice) -> Unit,
    onAddContact: () -> Unit
) {
    val colors = peerDropColors
    LazyColumn(Modifier.fillMaxSize()) {
        item {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "PeerDrop",
                    fontSize   = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color      = colors.onBackground
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onAddContact) {
                    Icon(Icons.Default.PersonAdd, null, tint = colors.primary)
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
        color         = peerDropColors.subtle
    )
}

@Composable
fun PeerRow(peer: PeerDevice, onClick: () -> Unit) {
    val colors = peerDropColors
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(44.dp).clip(CircleShape)
                .background(if (peer.isOnline) colors.primaryDim else Color(0xFF1A1A1A)),
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
                color      = colors.onBackground,
                fontWeight = FontWeight.Medium,
                fontSize   = 15.sp
            )
            Text(
                text     = if (peer.isOnline) "Online" else "Offline",
                color    = if (peer.isOnline) colors.online else colors.subtle,
                fontSize = 12.sp
            )
        }
        Box(
            Modifier.size(8.dp).clip(CircleShape)
                .background(if (peer.isOnline) colors.online else colors.verySubtle)
        )
    }
}

@Composable
private fun EmptyContacts() {
    val colors = peerDropColors
    Column(
        Modifier.fillMaxWidth().padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("👤", fontSize = 40.sp)
        Spacer(Modifier.height(8.dp))
        Text("No contacts yet", color = colors.subtle, fontSize = 14.sp)
        Text("Tap + to add someone", color = colors.verySubtle, fontSize = 12.sp)
    }
}

// ── Send panel ────────────────────────────────────────────────────────────────

@Composable
private fun SendPanel(
    peer:        PeerDevice,
    sharedUris:  List<Uri>,
    onBack:      () -> Unit,
    onPickFiles: () -> Unit,
    onSendUris:  (List<Uri>) -> Unit,
    onForget:    () -> Unit
) {
    val colors = peerDropColors

    LaunchedEffect(peer, sharedUris) {
        if (sharedUris.isNotEmpty()) onSendUris(sharedUris)
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = colors.onBackground)
            }
            Box(
                Modifier.size(40.dp).clip(CircleShape)
                    .background(if (peer.isOnline) colors.primaryDim else colors.surfaceVar),
                Alignment.Center
            ) {
                Text(if (peer.isOnline) "🖥" else "💤", fontSize = 18.sp)
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    peer.displayName,
                    fontWeight = FontWeight.SemiBold,
                    color      = colors.onBackground
                )
                Text(
                    if (peer.isOnline) "Ready to receive" else "Offline",
                    color    = if (peer.isOnline) colors.online else colors.warning,
                    fontSize = 12.sp
                )
            }
            TextButton(onClick = onForget) {
                Text("Remove", color = colors.danger, fontSize = 12.sp)
            }
        }

        HorizontalDivider(color = colors.surfaceVar)

        Card(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            colors   = CardDefaults.cardColors(containerColor = colors.surface),
            shape    = RoundedCornerShape(16.dp),
            border   = androidx.compose.foundation.BorderStroke(2.dp, colors.border)
        ) {
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("⬇", fontSize = 44.sp, color = colors.verySubtle)
                Spacer(Modifier.height(12.dp))
                Text("Tap to pick files to send", color = colors.subtle, fontSize = 14.sp)
                Spacer(Modifier.height(16.dp))
                OutlinedButton(
                    onClick = onPickFiles,
                    enabled = peer.isOnline,
                    border  = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (peer.isOnline) colors.primary else colors.border
                    )
                ) {
                    Text(
                        "Browse Files",
                        color = if (peer.isOnline) colors.primary else colors.verySubtle
                    )
                }
            }
        }
    }
}

// ── Add Contact dialog ─────────────────────────────────────────────────────────

@Composable
private fun AddContactDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    val colors = peerDropColors
    var peerID by remember { mutableStateOf("") }
    val valid  = peerID.length == 64

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = colors.surface,
        title = {
            Text("Add Contact", color = colors.onBackground, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text("Paste the 64-character Peer ID", color = colors.subtle, fontSize = 13.sp)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value         = peerID,
                    onValueChange = { peerID = it.trim() },
                    placeholder   = {
                        Text("e.g. a1b2c3d4…", color = colors.verySubtle, fontSize = 12.sp)
                    },
                    singleLine    = true,
                    textStyle     = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace
                    ),
                    isError        = peerID.isNotEmpty() && !valid,
                    supportingText = {
                        if (peerID.isNotEmpty() && !valid)
                            Text("${peerID.length}/64 characters", fontSize = 11.sp)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = colors.primary,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor     = colors.onBackground,
                        unfocusedTextColor   = colors.onBackground,
                        errorBorderColor     = colors.warning,
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick  = { onAdd(peerID) },
                enabled  = valid,
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = colors.primary,
                    disabledContainerColor = colors.verySubtle
                )
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colors.subtle)
            }
        }
    )
}