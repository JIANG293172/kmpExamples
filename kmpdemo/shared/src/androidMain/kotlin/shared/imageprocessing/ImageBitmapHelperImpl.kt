package shared.imageprocessing

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.BitmapFactory

actual @Composable
fun rememberImageBitmap(imageData: ByteArray): ImageBitmap? {
    return try {
        BitmapFactory.decodeByteArray(imageData, 0, imageData.size)?.asImageBitmap()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}