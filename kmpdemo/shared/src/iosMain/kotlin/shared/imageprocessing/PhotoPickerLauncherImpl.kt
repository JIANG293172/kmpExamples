package shared.imageprocessing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * iOS PhotoPicker implementation using IosPhotoLibraryAccess
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberPhotoPickerLauncher(
    onImageSelected: (ByteArray?) -> Unit
): () -> Unit {
    return remember {
        {
            IosPhotoLibraryAccess.presentPicker(onImageSelected)
        }
    }
}