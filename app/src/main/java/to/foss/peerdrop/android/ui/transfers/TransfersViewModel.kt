package to.foss.peerdrop.android.ui.transfers

import androidx.lifecycle.ViewModel
import to.holepunch.peerdrop.android.data.repository.PeerDropRepository

class TransfersViewModel(private val repository: PeerDropRepository) : ViewModel() {

    val transfers = repository.transfers
}
