package to.foss.peerdrop.android.data.ipc

import org.json.JSONObject

/**
 * Encodes/decodes bare-rpc frames matching the exact compact-encoding wire format.
 *
 * Wire format (from bare-rpc/lib/messages.js):
 *
 * [4 bytes LE uint32: frame body length]
 * [body:]
 *   c.uint: type      (REQUEST=1, RESPONSE=2, STREAM=3)
 *   c.uint: id        (0 = event, >0 = request)
 *   -- for REQUEST --
 *   c.uint: command
 *   c.uint: stream    (0 for normal request/event)
 *   -- if stream==0 (hasData) --
 *   c.uint: data length (optionalBuffer)
 *   [data bytes]
 *
 * c.uint is compact-encoding variable-length uint:
 *   value < 0xFD          → 1 byte
 *   value < 0xFFFF        → 0xFD + 2 bytes LE uint16
 *   value < 0xFFFFFFFF    → 0xFE + 4 bytes LE uint32
 *   else                  → 0xFF + 8 bytes LE uint64
 *
 * c.uint32 is always 4 bytes LE (used only for the frame length prefix).
 *
 * c.optionalBuffer encodes as:
 *   null/undefined → c.uint(0)
 *   Buffer         → c.uint(byteLength) + bytes
 */
object RPCFrameCodec {

    private const val TYPE_REQUEST  = 1
    private const val TYPE_RESPONSE = 2

    // ── Compact uint encoding ─────────────────────────────────────────────────

    private fun encodeUint(value: Int): ByteArray {
        return when {
            value < 0xFD -> byteArrayOf(value.toByte())
            value <= 0xFFFF -> byteArrayOf(
                0xFD.toByte(),
                (value and 0xFF).toByte(),
                ((value shr 8) and 0xFF).toByte()
            )
            else -> byteArrayOf(
                0xFE.toByte(),
                (value and 0xFF).toByte(),
                ((value shr 8) and 0xFF).toByte(),
                ((value shr 16) and 0xFF).toByte(),
                ((value shr 24) and 0xFF).toByte()
            )
        }
    }

    private fun decodeUint(buf: ByteArray, offset: Int): Pair<Int, Int> {
        val first = buf[offset].toInt() and 0xFF
        return when {
            first < 0xFD -> Pair(first, offset + 1)
            first == 0xFD -> {
                val v = (buf[offset+1].toInt() and 0xFF) or
                        ((buf[offset+2].toInt() and 0xFF) shl 8)
                Pair(v, offset + 3)
            }
            first == 0xFE -> {
                val v = (buf[offset+1].toInt() and 0xFF) or
                        ((buf[offset+2].toInt() and 0xFF) shl 8) or
                        ((buf[offset+3].toInt() and 0xFF) shl 16) or
                        ((buf[offset+4].toInt() and 0xFF) shl 24)
                Pair(v, offset + 5)
            }
            else -> Pair(0, offset + 9) // 64-bit, skip
        }
    }

    private fun encodeUint32LE(value: Int): ByteArray = byteArrayOf(
        (value and 0xFF).toByte(),
        ((value shr 8) and 0xFF).toByte(),
        ((value shr 16) and 0xFF).toByte(),
        ((value shr 24) and 0xFF).toByte()
    )

    private fun decodeUint32LE(buf: ByteArray, offset: Int): Int =
        (buf[offset].toInt() and 0xFF) or
                ((buf[offset+1].toInt() and 0xFF) shl 8) or
                ((buf[offset+2].toInt() and 0xFF) shl 16) or
                ((buf[offset+3].toInt() and 0xFF) shl 24)

    // ── Encode ────────────────────────────────────────────────────────────────

    fun encodeEvent(command: Int, body: JSONObject): ByteArray =
        encode(id = 0, command = command, body = body)

    fun encodeRequest(id: Int, command: Int, body: JSONObject): ByteArray =
        encode(id = id, command = command, body = body)

    private fun encode(id: Int, command: Int, body: JSONObject): ByteArray {
        val data = body.toString().toByteArray(Charsets.UTF_8)

        // Build body: type + id + command + stream + optionalBuffer(data)
        val bodyBytes = mutableListOf<Byte>()

        // type = REQUEST (1)
        bodyBytes.addAll(encodeUint(TYPE_REQUEST).toList())
        // id
        bodyBytes.addAll(encodeUint(id).toList())
        // command
        bodyBytes.addAll(encodeUint(command).toList())
        // stream = 0
        bodyBytes.addAll(encodeUint(0).toList())
        // optionalBuffer: data length + data
        bodyBytes.addAll(encodeUint(data.size).toList())
        bodyBytes.addAll(data.toList())

        val bodyArr = bodyBytes.toByteArray()

        // Frame length prefix (uint32 LE) — length of body
        val packet = mutableListOf<Byte>()
        packet.addAll(encodeUint32LE(bodyArr.size).toList())
        packet.addAll(bodyArr.toList())

        return packet.toByteArray()
    }

    // ── Decode ────────────────────────────────────────────────────────────────

    data class Frame(
        val type:    Int,
        val id:      Int,
        val command: Int,
        val data:    JSONObject?
    )

    /**
     * Parse a raw frame body (without the 4-byte length prefix).
     * Returns null if parsing fails.
     */
    fun decodeFrame(body: ByteArray): Frame? {
        return try {
            var offset = 0

            val (type, o1) = decodeUint(body, offset); offset = o1
            val (id,   o2) = decodeUint(body, offset); offset = o2

            when (type) {
                TYPE_REQUEST -> {
                    val (command, o3) = decodeUint(body, offset); offset = o3
                    val (stream,  o4) = decodeUint(body, offset); offset = o4

                    val json = if (stream == 0) {
                        // optionalBuffer: read length then bytes
                        val (dataLen, o5) = decodeUint(body, offset); offset = o5
                        if (dataLen > 0 && offset + dataLen <= body.size) {
                            val dataBytes = body.copyOfRange(offset, offset + dataLen)
                            try { JSONObject(String(dataBytes, Charsets.UTF_8)) }
                            catch (e: Exception) { JSONObject() }
                        } else JSONObject()
                    } else JSONObject()

                    Frame(type, id, command, json)
                }
                TYPE_RESPONSE -> {
                    // We don't need to parse responses from JS in detail
                    // just return a minimal frame so requests can be resolved
                    Frame(type, id, command = 0, data = JSONObject())
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
}