package shared.imageprocessing

import androidx.compose.runtime.Composable

/**
 * 照片选择器启动器 - 跨平台统一接口
 *
 * Android: 使用 ActivityResultContracts
 * iOS: 使用 UIViewControllerRepresentable 封装 PHPickerViewController
 */
@Composable
expect fun rememberPhotoPickerLauncher(
    onImageSelected: (ByteArray?) -> Unit
): () -> Unit