package to.foss.bare.android.data.ipc

import android.content.Context
import android.content.res.AssetManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import to.holepunch.bare.kit.IPC
import to.holepunch.bare.kit.Worklet

/**
 * Manages the bare Worklet + IPC lifecycle.
 * Single instance — injected via Koin as a singleton.
 */
class IPCService(private val assets: AssetManager) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var worklet: Worklet? = null
    private var _ipc: IPC? = null
    val ipc: IPC get() = _ipc ?: error("IPCService not started")

    private val _incoming = MutableSharedFlow<ByteArray>(extraBufferCapacity = 64)
    val incoming: Flow<ByteArray> = _incoming.asSharedFlow()

    fun start() {
        worklet = Worklet(null).also { wl ->
            wl.start("/app.bundle", assets.open("app.bundle"), null)
            _ipc = IPC(wl)
            _ipc!!.readable {
                scope.launch { readLoop() }
            }
        }
    }

    fun suspend() {
        worklet?.suspend()
    }

    fun resume() {
        worklet?.resume()
    }

    fun destroy() {
        scope.cancel()
        worklet?.terminate()
        worklet = null
        _ipc = null
    }

    fun write(data: ByteArray) {
        scope.launch {
            _ipc?.write(java.nio.ByteBuffer.wrap(data))
        }
    }

    private suspend fun readLoop() {
        val ipc = _ipc ?: return
        val acc = mutableListOf<Byte>()

        ipc.readStream().collect { raw ->
            acc.addAll(raw.toByteArray(Charsets.ISO_8859_1).toList())
            drainFrames(acc)
        }
    }

    private suspend fun drainFrames(acc: MutableList<Byte>) {
        while (acc.size >= 4) {
            val lenBytes = acc.take(4).toByteArray()
            val frameLen = java.nio.ByteBuffer.wrap(lenBytes)
                .order(java.nio.ByteOrder.LITTLE_ENDIAN).int

            if (acc.size < 4 + frameLen) break

            val frame = acc.drop(4).take(frameLen).toByteArray()
            repeat(4 + frameLen) { acc.removeAt(0) }

            _incoming.emit(frame)
        }
    }
}
