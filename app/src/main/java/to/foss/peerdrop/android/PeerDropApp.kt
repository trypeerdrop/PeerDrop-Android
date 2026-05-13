package to.foss.peerdrop.android

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import to.foss.peerdrop.android.di.appModule

class PeerDropApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@PeerDropApp)
            modules(appModule)
        }
    }
}
