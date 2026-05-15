package to.foss.peerdrop.android.di

import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import to.foss.peerdrop.android.data.ipc.IPCService
import to.foss.peerdrop.android.data.repository.PeerDropRepository
import to.foss.peerdrop.android.ui.devices.DevicesViewModel
import to.foss.peerdrop.android.ui.settings.SettingsViewModel
import to.foss.peerdrop.android.ui.transfers.TransfersViewModel
import java.io.File

val appModule = module {

    single {
        val context     = androidContext()
        val filesDir    = context.filesDir
        val downloadDir = context.getExternalFilesDir(null)
            ?.let { File(it, "PeerDrop") }
            ?: File(context.filesDir, "PeerDrop")

        IPCService(
            assets      = context.assets,
            filesDir    = filesDir,
            downloadDir = downloadDir
        )
    }

    single { PeerDropRepository(get()) }

    viewModel { DevicesViewModel(get()) }
    viewModel { TransfersViewModel(get()) }
    viewModel { SettingsViewModel(get()) }
}