package to.foss.peerdrop.android.ui.settings

import androidx.lifecycle.ViewModel
import to.holepunch.peerdrop.android.data.repository.PeerDropRepository

class SettingsViewModel(private val repository: PeerDropRepository) : ViewModel() {

    val myPeerID = repository.myPeerID
    val ready    = repository.ready

    fun setDownloadPath(path: String) = repository.setDownloadPath(path)
}
