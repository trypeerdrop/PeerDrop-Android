package to.foss.peerdrop.android.data.ipc

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import to.holepunch.bare.kit.IPC
import java.nio.ByteBuffer
import java.nio.charset.Charset
import kotlin.coroutines.resume

/**
 * Extension functions for bare-kit IPC — Flow-based reading.
 */
object IPCUtils {

    fun IPC.readByteStream(): Flow<ByteArray> = callbackFlow {
        val cb = IPC.PollCallback {
            try {
                var buf: ByteBuffer?
                while (true) {
                    buf = this@readByteStream.read()
                    if (buf == null) break
                    val bytes = ByteArray(buf.remaining())
                    buf.get(bytes)
                    trySend(bytes)
                }
            } catch (e: Exception) {
                close(e)
            }
        }
        readable(cb)
        awaitClose { readable(null) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun IPC.writeAsync(data: ByteBuffer): Boolean {
        val written = write(data)
        if (written == data.limit()) return true

        // Use writable callback for backpressure
        return suspendCancellableCoroutine { cont ->
            val cb = IPC.PollCallback {
                val w = write(data)
                if (w == data.limit()) {
                    writable(null)
                    cont.resume(true)
                }
            }
            writable(cb)
            cont.invokeOnCancellation { writable(null) }
        }
    }
}
