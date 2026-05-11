package to.foss.peerdrop.android.data.repository

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import to.holepunch.peerdrop.android.data.ipc.IPCService
import to.holepunch.peerdrop.android.data.ipc.RPCFrameCodec
import to.holepunch.peerdrop.android.data.model.Cmd
import to.holepunch.peerdrop.android.data.model.FileTransfer
import to.holepunch.peerdrop.android.data.model.PeerDevice

/**
 * Single source of truth for PeerDrop state.
 * Observes IPCService.incoming and exposes StateFlows to ViewModels.
 */
class PeerDropRepository(private val ipcService: IPCService) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _myPeerID   = MutableStateFlow("")
    private val _peers      = MutableStateFlow<List<PeerDevice>>(emptyList())
    private val _transfers  = MutableStateFlow<List<FileTransfer>>(emptyList())
    private val _ready      = MutableStateFlow(false)

    val myPeerID:  StateFlow<String>           = _myPeerID.asStateFlow()
    val peers:     StateFlow<List<PeerDevice>> = _peers.asStateFlow()
    val transfers: StateFlow<List<FileTransfer>> = _transfers.asStateFlow()
    val ready:     StateFlow<Boolean>          = _ready.asStateFlow()

    private var nextRequestId = 1

    init {
        // Subscribe to incoming IPC frames
        ipcService.incoming
            .onEach { frame -> handleFrame(frame) }
            .launchIn(scope)
    }

    // ── Frame handler ─────────────────────────────────────────────────────────

    private fun handleFrame(frameBytes: ByteArray) {
        val frame = RPCFrameCodec.decodeFrame(frameBytes) ?: return
        if (frame.id != 0) return  // only handle events from JS

        when (frame.command) {
            Cmd.READY -> {
                _myPeerID.value = frame.data.optString("peerID")
                _ready.value    = true
                Log.d("PeerDrop", "Ready — peerID: ${_myPeerID.value}")
            }
            Cmd.SAVED_PEERS -> {
                _peers.value = parsePeers(frame.data.optJSONArray("peers"))
            }
            Cmd.PEER_CONNECTED -> {
                val dk = frame.data.optString("discoveryKey")
                val updated = PeerDevice(
                    discoveryKey = dk,
                    displayName  = frame.data.optString("displayName"),
                    platform     = frame.data.optString("platform"),
                    isOnline     = true,
                    isOwnDevice  = frame.data.optBoolean("isOwnDevice")
                )
                _peers.update { list ->
                    val idx = list.indexOfFirst { it.discoveryKey == dk }
                    if (idx >= 0) list.toMutableList().also { it[idx] = updated }
                    else list + updated
                }
            }
            Cmd.PEER_DISCONNECTED -> {
                val dk = frame.data.optString("discoveryKey")
                _peers.update { list ->
                    list.map { if (it.discoveryKey == dk) it.copy(isOnline = false) else it }
                }
            }
            Cmd.TRANSFER_STARTED -> {
                val t = FileTransfer(
                    id        = frame.data.optString("transferId"),
                    peerId    = frame.data.optString("peerId"),
                    fileName  = frame.data.optString("fileName"),
                    fileSize  = frame.data.optLong("fileSize"),
                    direction = if (frame.data.optString("direction") == "sending")
                        FileTransfer.Direction.SENDING
                    else
                        FileTransfer.Direction.RECEIVING
                )
                _transfers.update { it + t }
            }
            Cmd.TRANSFER_PROGRESS -> {
                val id = frame.data.optString("transferId")
                val p  = frame.data.optDouble("progress").toFloat()
                _transfers.update { list ->
                    list.map { if (it.id == id) it.copy(progress = p) else it }
                }
            }
            Cmd.TRANSFER_COMPLETE -> {
                val id = frame.data.optString("transferId")
                _transfers.update { list ->
                    list.map { if (it.id == id) it.copy(progress = 1f) else it }
                }
                scope.launch {
                    delay(3000)
                    _transfers.update { list -> list.filter { it.id != id } }
                }
            }
            Cmd.ERROR -> {
                Log.e("PeerDrop", "JS error: ${frame.data.optString("message")}")
            }
        }
    }

    // ── Commands ──────────────────────────────────────────────────────────────

    fun sendFile(filePath: String, peerId: String) {
        val body = JSONObject().apply {
            put("filePath", filePath)
            put("peerId", peerId)
        }
        sendEvent(Cmd.SEND_FILE, body)
    }

    fun connectPeer(peerID: String) {
        sendRequest(Cmd.CONNECT_PEER, JSONObject().apply { put("peerID", peerID) })
    }

    fun forgetPeer(discoveryKey: String) {
        sendRequest(Cmd.FORGET_PEER, JSONObject().apply { put("peerDiscoveryKey", discoveryKey) })
    }

    fun setDownloadPath(path: String) {
        sendRequest(Cmd.SET_DOWNLOAD_PATH, JSONObject().apply { put("downloadPath", path) })
    }

    // ── IPC write helpers ─────────────────────────────────────────────────────

    private fun sendEvent(command: Int, body: JSONObject) {
        ipcService.write(RPCFrameCodec.encodeEvent(command, body))
    }

    private fun sendRequest(command: Int, body: JSONObject) {
        ipcService.write(RPCFrameCodec.encodeRequest(nextRequestId++, command, body))
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun parsePeers(arr: JSONArray?): List<PeerDevice> {
        arr ?: return emptyList()
        return (0 until arr.length()).map { i ->
            val p = arr.getJSONObject(i)
            PeerDevice(
                discoveryKey = p.optString("discoveryKey"),
                displayName  = p.optString("displayName"),
                platform     = p.optString("platform"),
                isOnline     = p.optBoolean("isOnline"),
                isOwnDevice  = p.optBoolean("isOwnDevice")
            )
        }
    }
}
