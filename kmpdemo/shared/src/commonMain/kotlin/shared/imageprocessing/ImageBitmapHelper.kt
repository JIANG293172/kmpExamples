package shared.imageprocessing

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

/**
 * 从字节数组创建 ImageBitmap 的平台特定实现
 */
expect@Composable
fun rememberImageBitmap(imageData: ByteArray): ImageBitmap?