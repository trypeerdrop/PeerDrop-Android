package to.foss.peerdrop.android.ui.devices

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log

/**
 * Opens a content:// URI and returns an fd:// spec for bare-fs.
 * Format: "fd://5|filename.jpg|1234567"
 *
 * Uses detachFd() to transfer ownership to bare-fs — no double-close.
 */
object FileUtils {

    data class FdSpec(
        val spec:     String,
        val fileName: String,
        val fileSize: Long
    )

    fun openUri(context: Context, uri: Uri): FdSpec? {
        return try {
            val pfd      = context.contentResolver.openFileDescriptor(uri, "r") ?: return null
            val fileName = getFileName(context, uri) ?: "file_${System.currentTimeMillis()}"
            val fileSize = getFileSize(context, uri)

            // detachFd() transfers fd ownership to bare-fs
            // bare-fs closes it when the stream ends — no double-close
            val fd   = pfd.detachFd()
            val spec = "fd://$fd|$fileName|$fileSize"

            FdSpec(spec, fileName, fileSize)
        } catch (e: Exception) {
            Log.e("PeerDrop", "Failed to open URI: $e")
            null
        }
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var name: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) name = cursor.getString(idx)
            }
        }
        return name
    }

    private fun getFileSize(context: Context, uri: Uri): Long {
        var size = 0L
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idx = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (idx >= 0 && !cursor.isNull(idx)) size = cursor.getLong(idx)
            }
        }
        return size
    }
}