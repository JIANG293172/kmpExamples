package shared.imageprocessing

import kotlinx.cinterop.ExperimentalForeignApi

/**
 * iOS implementation of ImagePickerHelper
 *
 * Uses file-based IPC to communicate with Swift photo picker in iosApp.
 * Kotlin creates trigger file -> Swift detects it -> Swift shows picker ->
 * Swift saves image to result file -> Kotlin reads result and calls callback.
 */
@OptIn(ExperimentalForeignApi::class)
actual object ImagePickerHelper {
    actual fun pickImage(onResult: (ByteArray?) -> Unit) {
        // Use IosPhotoLibraryAccess for file-based picker communication
        // This creates trigger file, polls for result, and calls callback with image data
        IosPhotoLibraryAccess.presentPicker(onResult)
    }
}