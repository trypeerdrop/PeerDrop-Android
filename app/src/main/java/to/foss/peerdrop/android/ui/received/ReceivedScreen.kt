package to.foss.peerdrop.android.ui.received

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import java.io.File
import to.holepunch.peerdrop.android.ui.theme.PeerDropColors

@Composable
fun ReceivedScreen(contentPadding: PaddingValues) {
    val context = LocalContext.current
    var files   by remember { mutableStateOf<List<File>>(emptyList()) }

    fun refresh() {
        val dir = File(context.getExternalFilesDir(null), "PeerDrop")
        files = if (dir.exists()) dir.listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList()
                else emptyList()
    }

    LaunchedEffect(Unit) { refresh() }

    Box(Modifier.fillMaxSize().padding(contentPadding)) {
        if (files.isEmpty()) {
            Column(
                Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("📥", fontSize = 48.sp)
                Spacer(Modifier.height(8.dp))
                Text("No received files yet", color = PeerDropColors.Subtle)
                Text(
                    "Files sent to you appear here",
                    color    = PeerDropColors.VerySubtle,
                    fontSize = 12.sp
                )
            }
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                item {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Received",
                            fontSize   = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color      = PeerDropColors.OnBackground
                        )
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { refresh() }) {
                            Icon(Icons.Default.Refresh, null, tint = PeerDropColors.Subtle)
                        }
                    }
                }
                items(files, key = { it.absolutePath }) { file ->
                    ReceivedFileRow(file) {
                        // Open file
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            file
                        )
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, context.contentResolver.getType(uri))
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Open with"))
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceivedFileRow(file: File, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = PeerDropColors.Surface),
        shape  = RoundedCornerShape(12.dp)
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Description,
                null,
                tint     = PeerDropColors.Primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    file.name,
                    color      = PeerDropColors.OnBackground,
                    fontWeight = FontWeight.Medium,
                    fontSize   = 14.sp,
                    maxLines   = 1
                )
                Text(
                    formatSize(file.length()),
                    color    = PeerDropColors.Subtle,
                    fontSize = 12.sp
                )
            }
            Text("›", color = PeerDropColors.Subtle, fontSize = 20.sp)
        }
    }
}

private fun formatSize(bytes: Long): String = when {
    bytes < 1024       -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else               -> "${bytes / (1024 * 1024)} MB"
}
