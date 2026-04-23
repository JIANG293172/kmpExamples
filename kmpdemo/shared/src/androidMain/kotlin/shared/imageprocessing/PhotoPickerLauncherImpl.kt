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

/**
 * Android implementation of PhotoPickerLauncher using ActivityResultContracts
 */
@Composable
actual fun rememberPhotoPickerLauncher(
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

    return remember { { launcher.launch("image/*") } }
}

private fun loadImageFromUri(context: Context, uri: Uri): ByteArray? {
    return try {
        // Try to get persistent URI permission
        try {
            val flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, flags)
        } catch (e: SecurityException) {
            // Fall back to temporary permission
        }

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val bitmap = BitmapFactory.decodeStream(inputStream)
            if (bitmap != null) {
                val outputStream = java.io.ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
                bitmap.recycle()
                outputStream.toByteArray()
            } else null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}