package shared.imageprocessing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

actual object ImagePickerHelper {

    actual fun pickImage(onResult: (ByteArray?) -> Unit) {
        // 这个方法需要在Composable环境中调用
        // 实际实现在 rememberLauncher 中
        onResult(null)
    }
}

@Composable
fun rememberImagePickerLauncher(
    onImageSelected: (ByteArray?) -> Unit
): () -> Unit {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val imageData = loadImageFromUri(context, uri)
            onImageSelected(imageData)
        } else {
            onImageSelected(null)
        }
    }

    return { launcher.launch("image/*") }
}

private fun loadImageFromUri(context: Context, uri: Uri): ByteArray? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val bitmap = BitmapFactory.decodeStream(inputStream)
            bitmap?.let {
                val outputStream = java.io.ByteArrayOutputStream()
                it.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                it.recycle()
                outputStream.toByteArray()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}