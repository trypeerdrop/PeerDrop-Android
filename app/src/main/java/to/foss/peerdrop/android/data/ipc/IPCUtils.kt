package to.foss.bare.android.data.ipc

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import to.holepunch.bare.kit.IPC
import java.nio.ByteBuffer
import java.nio.charset.Charset

/**
 * Extension functions for bare-kit IPC — Flow-based reading.
 */
object IPCUtils {

    fun IPC.readStream(): Flow<String> = callbackFlow {
        val utf8 = Charset.forName("UTF-8")
        val cb = IPC.PollCallback {
            try {
                var buf: ByteBuffer?
                while (true) {
                    buf = this@readStream.read()
                    if (buf == null) break
                    trySend(utf8.decode(buf).toString())
                }
            } catch (e: Exception) {
                close(e)
            }
        }
        readable(cb)
        awaitClose { readable(null) }
    }

    suspend fun IPC.writeAsync(data: ByteBuffer): Boolean {
        val written = write(data)
        if (written == data.limit()) return true

        // Use writable callback for backpressure
        return kotlinx.coroutines.suspendCancellableCoroutine { cont ->
            val cb = IPC.PollCallback {
                val w = write(data)
                if (w == data.limit()) {
                    writable(null)
                    cont.resume(true) {}
                }
            }
            writable(cb)
            cont.invokeOnCancellation { writable(null) }
        }
    }
}
