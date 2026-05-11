package to.foss.peerdrop.android.ui.transfers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import to.holepunch.peerdrop.android.data.model.FileTransfer
import to.holepunch.peerdrop.android.ui.theme.PeerDropColors

@Composable
fun TransfersScreen(
    contentPadding: PaddingValues,
    viewModel: TransfersViewModel = koinViewModel()
) {
    val transfers by viewModel.transfers.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxSize().padding(contentPadding)) {
        if (transfers.isEmpty()) {
            Column(
                Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("↕", fontSize = 48.sp, color = PeerDropColors.VerySubtle)
                Spacer(Modifier.height(8.dp))
                Text("No active transfers", color = PeerDropColors.Subtle)
            }
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                item {
                    Text(
                        "Transfers",
                        modifier   = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                        fontSize   = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color      = PeerDropColors.OnBackground
                    )
                }
                items(transfers, key = { it.id }) { transfer ->
                    TransferCard(transfer)
                }
            }
        }
    }
}

@Composable
fun TransferCard(transfer: FileTransfer) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = PeerDropColors.Surface),
        shape  = RoundedCornerShape(12.dp)
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isSending = transfer.direction == FileTransfer.Direction.SENDING
            Text(
                if (isSending) "↑" else "↓",
                color      = if (isSending) PeerDropColors.SendBlue else PeerDropColors.Online,
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    transfer.fileName,
                    color    = PeerDropColors.OnBackground,
                    fontSize = 14.sp,
                    maxLines = 1
                )
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress    = { transfer.progress },
                    modifier    = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color       = if (isSending) PeerDropColors.SendBlue else PeerDropColors.Online,
                    trackColor  = PeerDropColors.SurfaceVar
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                "${(transfer.progress * 100).toInt()}%",
                color    = PeerDropColors.Subtle,
                fontSize = 12.sp
            )
        }
    }
}
