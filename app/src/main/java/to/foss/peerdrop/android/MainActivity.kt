package to.foss.peerdrop.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.core.view.WindowCompat
import org.koin.android.ext.android.inject
import to.foss.peerdrop.android.data.ipc.IPCService
import to.foss.peerdrop.android.ui.PeerDropTheme
import to.foss.peerdrop.android.ui.RootScreen

class MainActivity : ComponentActivity() {

    private val ipcService: IPCService by inject()

    private var onFilesPicked: ((List<Uri>) -> Unit)? = null
    private val filePicker = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris -> onFilesPicked?.invoke(uris) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ipcService.start()

        val sharedUris: List<Uri> = when (intent?.action) {
            Intent.ACTION_SEND -> {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    listOfNotNull(intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java))
                } else {
                    @Suppress("DEPRECATION")
                    listOfNotNull(intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM))
                }
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java) ?: emptyList()
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM) ?: emptyList()
                }
            }
            else -> emptyList()
        }

        setContent {
            PeerDropTheme {
                // Update status bar icons based on theme
                val isDark = isSystemInDarkTheme()
                SideEffect {
                    WindowCompat.getInsetsController(window, window.decorView)
                        .isAppearanceLightStatusBars = !isDark
                }

                RootScreen(
                    sharedUris  = sharedUris,
                    onPickFiles = { callback ->
                        onFilesPicked = callback
                        filePicker.launch("*/*")
                    }
                )
            }
        }
    }

    override fun onPause()   { super.onPause();   ipcService.suspend() }
    override fun onResume()  { super.onResume();  ipcService.resume()  }
    override fun onDestroy() { super.onDestroy(); ipcService.destroy() }
}