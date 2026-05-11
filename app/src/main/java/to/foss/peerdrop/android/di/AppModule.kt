package to.foss.peerdrop.android.di

import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import to.holepunch.peerdrop.android.data.ipc.IPCService
import to.holepunch.peerdrop.android.data.repository.PeerDropRepository
import to.holepunch.peerdrop.android.ui.devices.DevicesViewModel
import to.holepunch.peerdrop.android.ui.settings.SettingsViewModel
import to.holepunch.peerdrop.android.ui.transfers.TransfersViewModel

val appModule = module {

    // IPCService — singleton, owns worklet + IPC lifecycle
    single { IPCService(androidContext().assets) }

    // Repository — singleton, subscribes to IPCService.incoming
    single { PeerDropRepository(get()) }

    // ViewModels — one per screen, all share the same repository
    viewModel { DevicesViewModel(get()) }
    viewModel { TransfersViewModel(get()) }
    viewModel { SettingsViewModel(get()) }
}
