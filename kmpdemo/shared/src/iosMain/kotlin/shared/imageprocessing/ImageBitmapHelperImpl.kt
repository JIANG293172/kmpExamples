package shared.imageprocessing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap

/**
 * iOS implementation of rememberImageBitmap
 *
 * Due to Kotlin/Native iOS interop limitations with CGImage APIs,
 * this returns null which shows a placeholder.
 *
 * The image data IS correctly passed from Swift (file IPC works),
 * but we cannot decode it to ImageBitmap due to Kotlin/Native iOS CGImage limitations.
 */
actual @Composable
fun rememberImageBitmap(imageData: ByteArray): ImageBitmap? {
    return remember(imageData) {
        // Return null to show placeholder
        // The actual image display is handled by a SwiftUI overlay in iosApp
        null
    }
}