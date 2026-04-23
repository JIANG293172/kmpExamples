package shared.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSTemporaryDirectory
import platform.UIKit.UIImage
import platform.UIKit.UIImageView
import platform.UIKit.UIColor
import platform.UIKit.UIViewContentMode

/**
 * iOS implementation of NativeImageDisplay using UIKit's UIImageView
 *
 * This loads the image directly from the result file that Swift saves.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun NativeImageDisplay(
    imageData: ByteArray?,
    modifier: Modifier,
    onClick: () -> Unit
) {
    // The image is saved by Swift to kmp_photo_result.jpg
    // We load it directly from there using UIKit
    val resultFilePath = "${NSTemporaryDirectory()}kmp_photo_result.jpg"

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        UIKitView(
            factory = {
                UIImageView().apply {
                    contentMode = UIViewContentMode.UIViewContentModeScaleAspectFit
                    clipsToBounds = true
                    backgroundColor = UIColor.clearColor
                    userInteractionEnabled = true
                    // Load image directly from the file path
                    image = UIImage(contentsOfFile = resultFilePath)
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                // Reload image when composable updates
                view.image = UIImage(contentsOfFile = resultFilePath)
            }
        )
    }
}