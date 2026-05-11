package to.foss.peerdrop.android.ui.devices

import androidx.lifecycle.ViewModel
import to.holepunch.peerdrop.android.data.repository.PeerDropRepository

class DevicesViewModel(private val repository: PeerDropRepository) : ViewModel() {

    val peers = repository.peers
    val ready = repository.ready

    fun connectPeer(peerID: String)            = repository.connectPeer(peerID)
    fun forgetPeer(discoveryKey: String)       = repository.forgetPeer(discoveryKey)
    fun sendFile(path: String, peerId: String) = repository.sendFile(path, peerId)
}
