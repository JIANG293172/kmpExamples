package shared.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * Native image display component that works on both Android and iOS.
 *
 * On Android: Uses standard ImageBitmap display.
 * On iOS: Uses native SwiftUI ImageView via UIKitView.
 */
expect @Composable
fun NativeImageDisplay(
    imageData: ByteArray?,
    modifier: Modifier,
    onClick: () -> Unit
)