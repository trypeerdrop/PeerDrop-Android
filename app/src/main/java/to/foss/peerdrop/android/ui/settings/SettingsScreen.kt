package to.foss.peerdrop.android.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import to.holepunch.peerdrop.android.ui.theme.PeerDropColors

@Composable
fun SettingsScreen(
    contentPadding: PaddingValues,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val myPeerID by viewModel.myPeerID.collectAsStateWithLifecycle()
    val ready    by viewModel.ready.collectAsStateWithLifecycle()
    val clipboard = LocalClipboardManager.current
    var downloadPath by remember { mutableStateOf("PeerDrop") }
    var copied       by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 20.dp)
    ) {
        Text(
            "Settings",
            modifier   = Modifier.padding(vertical = 16.dp),
            fontSize   = 28.sp,
            fontWeight = FontWeight.Bold,
            color      = PeerDropColors.OnBackground
        )

        // ── Status ────────────────────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Surface(
                modifier = Modifier.size(8.dp),
                shape    = RoundedCornerShape(50),
                color    = if (ready) PeerDropColors.Online else PeerDropColors.Warning
            ) {}
            Spacer(Modifier.width(8.dp))
            Text(
                if (ready) "Connected to network" else "Connecting…",
                color    = PeerDropColors.Subtle,
                fontSize = 13.sp
            )
        }

        // ── Peer ID ───────────────────────────────────────────────────────────
        SectionLabel("MY PEER ID")
        Spacer(Modifier.height(8.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0D18)),
            shape  = RoundedCornerShape(12.dp)
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Text(
                    myPeerID.ifEmpty { "Loading…" },
                    color      = PeerDropColors.Subtle,
                    fontFamily = FontFamily.Monospace,
                    fontSize   = 12.sp,
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            if (myPeerID.isNotEmpty()) {
                                clipboard.setText(AnnotatedString(myPeerID))
                                copied = true
                            }
                        },
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, PeerDropColors.Primary
                        )
                    ) {
                        Text(
                            if (copied) "Copied!" else "Copy ID",
                            color = PeerDropColors.Primary,
                            fontSize = 13.sp
                        )
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
                placeholder   = { Text("PeerDrop", color = PeerDropColors.VerySubtle) },
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = PeerDropColors.Primary,
                    unfocusedBorderColor = PeerDropColors.Border,
                    focusedTextColor     = PeerDropColors.OnBackground,
                    unfocusedTextColor   = PeerDropColors.OnBackground,
                )
            )
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = { viewModel.setDownloadPath(downloadPath) },
                colors  = ButtonDefaults.buttonColors(containerColor = PeerDropColors.Primary)
            ) { Text("Set") }
        }

        Spacer(Modifier.height(24.dp))

        // ── About ─────────────────────────────────────────────────────────────
        SectionLabel("ABOUT")
        Spacer(Modifier.height(8.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = PeerDropColors.Surface),
            shape  = RoundedCornerShape(12.dp)
        ) {
            Column(Modifier.fillMaxWidth()) {
                SettingsRow("Version", "1.0.0")
                HorizontalDivider(color = PeerDropColors.SurfaceVar)
                SettingsRow("Platform", "Android")
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        fontSize      = 11.sp,
        fontWeight    = FontWeight.SemiBold,
        letterSpacing = 1.sp,
        color         = PeerDropColors.Subtle
    )
}

@Composable
private fun SettingsRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(label, color = PeerDropColors.OnBackground)
        Text(value, color = PeerDropColors.Subtle, fontSize = 14.sp)
    }
}
