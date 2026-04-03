package shared.imageprocessing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

actual class ImageLoader {

    actual suspend fun loadFromUri(uri: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            // 处理file:// URI
            val path = if (uri.startsWith("file://")) {
                uri.removePrefix("file://")
            } else {
                uri
            }
            loadFromPath(path)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    actual suspend fun loadFromPath(path: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            if (!file.exists()) return@withContext null

            val fileInputStream = FileInputStream(file)
            val bytes = fileInputStream.readBytes()
            fileInputStream.close()
            bytes
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    actual suspend fun saveToPath(data: ByteArray, path: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            file.parentFile?.mkdirs()

            val fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(data)
            fileOutputStream.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}