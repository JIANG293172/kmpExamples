package shared.imageprocessing

/**
 * iOS平台的图片加载器 - 简化版
 * 由于cinterop的复杂性，目前只是透传数据
 */
actual class ImageLoader {

    actual suspend fun loadFromUri(uri: String): ByteArray? {
        // iOS上需要原生代码来加载图片
        // 这里返回null，实际实现需要通过Expect/Actual桥接原生API
        return null
    }

    actual suspend fun loadFromPath(path: String): ByteArray? {
        return null
    }

    actual suspend fun saveToPath(data: ByteArray, path: String): Boolean {
        return false
    }
}