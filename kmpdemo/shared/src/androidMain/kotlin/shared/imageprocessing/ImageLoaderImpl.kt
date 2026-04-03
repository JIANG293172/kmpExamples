package shared.imageprocessing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

actual class ImageLoader {

    private val context: Context? = getAndroidContext()

    actual suspend fun loadFromUri(uri: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val androidContext = context ?: return@withContext null

            val parsedUri = Uri.parse(uri)
            androidContext.contentResolver.openInputStream(parsedUri)?.use { inputStream ->
                val bitmap = BitmapFactory.decodeStream(inputStream)
                bitmap?.let {
                    val outputStream = ByteArrayOutputStream()
                    it.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    it.recycle()
                    return@withContext outputStream.toByteArray()
                }
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    actual suspend fun loadFromPath(path: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            if (!file.exists()) return@withContext null

            val bitmap = BitmapFactory.decodeFile(path) ?: return@withContext null

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            bitmap.recycle()

            outputStream.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    actual suspend fun saveToPath(data: ByteArray, path: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            file.parentFile?.mkdirs()
            FileOutputStream(file).use { fos ->
                fos.write(data)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

// Android平台需要获取Context
private fun getAndroidContext(): Context? {
    return try {
        val activityClass = Class.forName("android.app.ActivityThread")
        val method = activityClass.getMethod("currentApplication")
        method.invoke(null) as? Context
    } catch (e: Exception) {
        null
    }
}