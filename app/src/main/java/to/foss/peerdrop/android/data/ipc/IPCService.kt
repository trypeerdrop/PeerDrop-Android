package to.foss.peerdrop.android.data.ipc

import android.content.res.AssetManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import to.foss.peerdrop.android.data.ipc.IPCUtils.readByteStream
import to.holepunch.bare.kit.IPC
import to.holepunch.bare.kit.Worklet
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

class IPCService(
    private val assets:      AssetManager,
    private val filesDir:    File,
    val          downloadDir: File
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var worklet: Worklet? = null
    private var _ipc:    IPC?     = null

    private val _incoming = MutableSharedFlow<ByteArray>(extraBufferCapacity = 64)
    val incoming: Flow<ByteArray> = _incoming.asSharedFlow()

    fun start() {
        worklet = Worklet(null).also { wl ->
            // Pass filesDir as argv[0] — accessible in JS as Bare.argv[0]
            wl.start(
                "/app.bundle",
                assets.open("app.bundle"),
                arrayOf(filesDir.absolutePath)
            )
            _ipc = IPC(wl)
            _ipc!!.readable {
                scope.launch { readLoop() }
            }
        }
    }

    fun suspend() { worklet?.suspend() }
    fun resume()  { worklet?.resume()  }

    fun destroy() {
        scope.cancel()
        worklet?.terminate()
        worklet = null
        _ipc    = null
    }

    fun write(data: ByteArray) {
        scope.launch {
            try { _ipc?.write(ByteBuffer.wrap(data)) }
            catch (e: Exception) { e.printStackTrace() }
        }
    }

    private suspend fun readLoop() {
        val ipc = _ipc ?: return
        val acc = mutableListOf<Byte>()

        ipc.readByteStream().collect { bytes ->
            acc.addAll(bytes.toList())
            drainFrames(acc)
        }
    }

    private suspend fun drainFrames(acc: MutableList<Byte>) {
        while (acc.size >= 4) {
            val lenBytes = acc.take(4).toByteArray()
            val frameLen = ByteBuffer.wrap(lenBytes)
                .order(ByteOrder.LITTLE_ENDIAN).int

            if (acc.size < 4 + frameLen) break

            val frame = acc.drop(4).take(frameLen).toByteArray()
            repeat(4 + frameLen) { acc.removeAt(0) }

            _incoming.emit(frame)
        }
    }
}