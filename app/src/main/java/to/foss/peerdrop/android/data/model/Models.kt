package to.foss.peerdrop.android.data.model

// ── Commands — must match commands.js ────────────────────────────────────────
object Cmd {
    const val READY             = 1
    const val PEER_CONNECTED    = 2
    const val PEER_DISCONNECTED = 3
    const val TRANSFER_STARTED  = 4
    const val TRANSFER_PROGRESS = 5
    const val TRANSFER_COMPLETE = 6
    const val ERROR             = 7
    const val SEND_FILE         = 8  // event (no reply)
    const val CONNECT_PEER      = 9
    const val SET_DOWNLOAD_PATH = 10
    const val SAVED_PEERS       = 11
    const val FORGET_PEER       = 12
}

data class PeerDevice(
    val discoveryKey: String,
    val displayName:  String,
    val platform:     String,
    val isOnline:     Boolean,
    val isOwnDevice:  Boolean
)

data class FileTransfer(
    val id:        String,
    val peerId:    String,
    val fileName:  String,
    val fileSize:  Long,
    val direction: Direction,
    val progress:  Float = 0f
) {
    enum class Direction { SENDING, RECEIVING }
}
