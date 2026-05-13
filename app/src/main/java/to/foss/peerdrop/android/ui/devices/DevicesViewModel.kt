package to.foss.peerdrop.android.ui.devices

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import to.foss.peerdrop.android.data.repository.PeerDropRepository

class DevicesViewModel(private val repository: PeerDropRepository) : ViewModel() {

    val peers = repository.peers
    val ready = repository.ready

    fun connectPeer(peerID: String)      = repository.connectPeer(peerID)
    fun forgetPeer(discoveryKey: String) = repository.forgetPeer(discoveryKey)

    /**
     * Send files from content:// URIs using fd:// spec.
     * detachFd() transfers ownership to bare-fs — no copy, no double-close.
     */
    fun sendFiles(context: Context, uris: List<Uri>, peerId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            uris.forEach { uri ->
                val fdSpec = FileUtils.openUri(context, uri)
                if (fdSpec != null) {
                    repository.sendFile(fdSpec.spec, peerId)
                } else {
                    android.util.Log.e("PeerDrop", "Failed to open URI: $uri")
                }
            }
        }
    }
}