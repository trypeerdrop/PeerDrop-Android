package to.foss.peerdrop.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import org.koin.android.ext.android.inject
import to.holepunch.peerdrop.android.data.ipc.IPCService
import to.holepunch.peerdrop.android.ui.root.RootScreen
import to.holepunch.peerdrop.android.ui.theme.PeerDropTheme

class MainActivity : ComponentActivity() {

    // IPCService injected by Koin — singleton
    private val ipcService: IPCService by inject()

    // File picker for send flow
    private var onFilesPicked: ((List<Uri>) -> Unit)? = null
    private val filePicker = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris -> onFilesPicked?.invoke(uris) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start bare worklet
        ipcService.start()

        // Extract shared files if launched via share intent
        val sharedUris: List<Uri> = when (intent?.action) {
            Intent.ACTION_SEND -> listOfNotNull(
                intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            )

            Intent.ACTION_SEND_MULTIPLE ->
                intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM) ?: emptyList()

            else -> emptyList()
        }

        setContent {
            PeerDropTheme {
                RootScreen(
                    sharedUris = sharedUris,
                    onPickFiles = { callback ->
                        onFilesPicked = callback
                        filePicker.launch("*/*")
                    }
                )
            }
        }
    }

    override fun onPause() {
        super.onPause(); ipcService.suspend()
    }

    override fun onResume() {
        super.onResume(); ipcService.resume()
    }

    override fun onDestroy() {
        super.onDestroy(); ipcService.destroy()
    }
}
