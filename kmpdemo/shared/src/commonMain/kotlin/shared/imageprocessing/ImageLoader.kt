package shared.imageprocessing

/**
 * 图片加载器接口 - KMP expect声明
 * 用于从各种来源（URI、文件路径等）加载图片为字节数组
 */
expect class ImageLoader {

    /**
     * 从URI加载图片
     * @param uri 图片URI（可以是file://、content://等）
     * @return 图片字节数组，失败返回null
     */
    suspend fun loadFromUri(uri: String): ByteArray?

    /**
     * 从文件路径加载图片
     * @param path 文件路径
     * @return 图片字节数组，失败返回null
     */
    suspend fun loadFromPath(path: String): ByteArray?

    /**
     * 将字节数组保存到文件
     * @param data 图片字节数组
     * @param path 保存路径
     * @return 是否保存成功
     */
    suspend fun saveToPath(data: ByteArray, path: String): Boolean
}

/**
 * 创建默认的图片加载器
 */
fun createImageLoader(): ImageLoader = ImageLoader()