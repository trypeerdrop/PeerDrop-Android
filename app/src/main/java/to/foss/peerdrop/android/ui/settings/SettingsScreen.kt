package to.foss.peerdrop.android.ui.settings

import android.content.ClipData
import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import to.foss.peerdrop.android.ui.peerDropColors

@Composable
fun SettingsScreen(
    contentPadding: PaddingValues,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val colors    = peerDropColors
    val myPeerID  by viewModel.myPeerID.collectAsStateWithLifecycle()
    val ready     by viewModel.ready.collectAsStateWithLifecycle()
    val clipboard  = LocalClipboard.current
    val scope     = rememberCoroutineScope()

    var downloadPath by remember { mutableStateOf("PeerDrop") }
    var copied       by remember { mutableStateOf(false) }
    var showQR       by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Settings",
            modifier   = Modifier.padding(vertical = 16.dp),
            fontSize   = 28.sp,
            fontWeight = FontWeight.Bold,
            color      = colors.onBackground
        )

        // ── Status ────────────────────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Surface(
                modifier = Modifier.size(8.dp),
                shape    = RoundedCornerShape(50),
                color    = if (ready) colors.online else colors.warning
            ) {}
            Spacer(Modifier.width(8.dp))
            Text(
                if (ready) "Connected to network" else "Connecting…",
                color    = colors.subtle,
                fontSize = 13.sp
            )
        }

        // ── Peer ID ───────────────────────────────────────────────────────────
        SectionLabel("MY PEER ID")
        Spacer(Modifier.height(8.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            shape  = RoundedCornerShape(12.dp)
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Text(
                    myPeerID.ifEmpty { "Loading…" },
                    color      = colors.subtle,
                    fontFamily = FontFamily.Monospace,
                    fontSize   = 11.sp,
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            if (myPeerID.isNotEmpty()) {
                                scope.launch {
                                    clipboard.setClipEntry(
                                        ClipData.newPlainText("Peer ID", myPeerID).toClipEntry()
                                    )
                                }
                                copied = true
                            }
                        },
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.primary)
                    ) {
                        Text(
                            if (copied) "Copied!" else "Copy ID",
                            color    = colors.primary,
                            fontSize = 13.sp
                        )
                    }
                    OutlinedButton(
                        onClick = { if (myPeerID.isNotEmpty()) showQR = true },
                        border  = androidx.compose.foundation.BorderStroke(1.dp, colors.primary)
                    ) {
                        Text("Show QR", color = colors.primary, fontSize = 13.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Download path ─────────────────────────────────────────────────────
        SectionLabel("DOWNLOAD FOLDER")
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value         = downloadPath,
                onValueChange = { downloadPath = it },
                modifier      = Modifier.weight(1f),
                singleLine    = true,
                placeholder   = { Text("PeerDrop", color = colors.verySubtle) },
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = colors.primary,
                    unfocusedBorderColor = colors.border,
                    focusedTextColor     = colors.onBackground,
                    unfocusedTextColor   = colors.onBackground,
                )
            )
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = { viewModel.setDownloadPath(downloadPath) },
                colors  = ButtonDefaults.buttonColors(containerColor = colors.primary)
            ) { Text("Set") }
        }

        Spacer(Modifier.height(24.dp))

        // ── About ─────────────────────────────────────────────────────────────
        SectionLabel("ABOUT")
        Spacer(Modifier.height(8.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            shape  = RoundedCornerShape(12.dp)
        ) {
            Column(Modifier.fillMaxWidth()) {
                SettingsRow("Version", "1.0.0")
                HorizontalDivider(color = colors.surfaceVar)
                SettingsRow("Platform", "Android")
            }
        }

        Spacer(Modifier.height(24.dp))
    }

    if (showQR && myPeerID.isNotEmpty()) {
        QRDialog(
            peerID    = "peerdrop://connect?id=$myPeerID",
            onDismiss = { showQR = false }
        )
    }
}

@Composable
private fun QRDialog(peerID: String, onDismiss: () -> Unit) {
    val colors   = peerDropColors
    val qrBitmap = remember(peerID) { generateQR(peerID) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            shape  = RoundedCornerShape(16.dp)
        ) {
            Column(
                Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Scan to Connect",
                    fontWeight = FontWeight.Bold,
                    color      = colors.onBackground,
                    fontSize   = 18.sp
                )
                Spacer(Modifier.height(16.dp))
                if (qrBitmap != null) {
                    androidx.compose.foundation.Image(
                        bitmap             = qrBitmap.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier           = Modifier.size(240.dp)
                    )
                } else {
                    Box(Modifier.size(240.dp), Alignment.Center) {
                        Text("Could not generate QR", color = colors.subtle)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("Ask your contact to scan this", color = colors.subtle, fontSize = 13.sp)
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = onDismiss) {
                    Text("Close", color = colors.primary)
                }
            }
        }
    }
}

private fun generateQR(content: String, size: Int = 512): Bitmap? {
    return try {
        val bits = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
        val bmp  = createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bmp[x, y] = if (bits[x, y]) Color.BLACK else Color.WHITE
            }
        }
        bmp
    } catch (e: Exception) { null }
}

@Composable
private fun SectionLabel(text: String) {
    val colors = peerDropColors
    Text(
        text,
        fontSize      = 11.sp,
        fontWeight    = FontWeight.SemiBold,
        letterSpacing = 1.sp,
        color         = colors.subtle
    )
}

@Composable
private fun SettingsRow(label: String, value: String) {
    val colors = peerDropColors
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(label, color = colors.onBackground)
        Text(value, color = colors.subtle, fontSize = 14.sp)
    }
}