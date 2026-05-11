package to.foss.peerdrop.android.data.ipc

import org.json.JSONObject
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Encodes/decodes bare-rpc wire frames.
 *
 * Frame format:
 *   [4 bytes LE: body length]
 *   [body:]
 *     [1 byte:  type      ]  — 0x01 = REQUEST
 *     [4 bytes: id     LE ]  — 0 = event, >0 = request
 *     [4 bytes: command LE]
 *     [4 bytes: stream  LE]  — 0
 *     [n bytes: JSON data ]
 */
object RPCFrameCodec {

    private const val HEADER_SIZE = 13 // type(1) + id(4) + command(4) + stream(4)

    // ── Encode ────────────────────────────────────────────────────────────────

    fun encodeEvent(command: Int, body: JSONObject): ByteArray =
        encode(id = 0, command = command, body = body)

    fun encodeRequest(id: Int, command: Int, body: JSONObject): ByteArray =
        encode(id = id, command = command, body = body)

    private fun encode(id: Int, command: Int, body: JSONObject): ByteArray {
        val data      = body.toString().toByteArray(Charsets.UTF_8)
        val frameSize = HEADER_SIZE + data.size
        val buf       = ByteBuffer.allocate(4 + frameSize).order(ByteOrder.LITTLE_ENDIAN)

        buf.putInt(frameSize)   // length prefix
        buf.put(0x01.toByte())  // type = REQUEST
        buf.putInt(id)
        buf.putInt(command)
        buf.putInt(0)           // stream = 0
        buf.put(data)

        return buf.array()
    }

    // ── Decode ────────────────────────────────────────────────────────────────

    data class Frame(
        val id:      Int,
        val command: Int,
        val data:    JSONObject
    )

    /** Parse a raw body (without the 4-byte length prefix). */
    fun decodeFrame(body: ByteArray): Frame? {
        if (body.size < HEADER_SIZE) return null

        val buf     = ByteBuffer.wrap(body).order(ByteOrder.LITTLE_ENDIAN)
        buf.get()                       // type — skip
        val id      = buf.int
        val command = buf.int
        buf.int                         // stream — skip

        val dataBytes = ByteArray(body.size - HEADER_SIZE)
        buf.get(dataBytes)

        val json = if (dataBytes.isEmpty()) JSONObject()
        else try { JSONObject(String(dataBytes, Charsets.UTF_8)) }
             catch (e: Exception) { return null }

        return Frame(id, command, json)
    }
}
