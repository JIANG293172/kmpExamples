package shared.imageprocessing

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import shared.data.BackgroundColor

/**
 * 图片处理结果
 */
data class ProcessResult(
    val success: Boolean,
    val data: ByteArray? = null,
    val errorMessage: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProcessResult) return false
        if (success != other.success) return false
        if (data != null) {
            if (other.data == null) return false
            if (!data.contentEquals(other.data)) return false
        } else if (other.data != null) return false
        if (errorMessage != other.errorMessage) return false
        return true
    }

    override fun hashCode(): Int {
        var result = success.hashCode()
        result = 31 * result + (data?.contentHashCode() ?: 0)
        result = 31 * result + (errorMessage?.hashCode() ?: 0)
        return result
    }
}

/**
 * 图像美化参数
 */
data class EnhanceParams(
    val brightness: Float = 0f,      // -1f ~ 1f
    val contrast: Float = 0f,        // -1f ~ 1f
    val sharpness: Float = 0f,      // 0f ~ 1f
    val denoise: Float = 0f,        // 0f ~ 1f
    val exposure: Float = 0f       // -0.2f ~ 0.2f
)

/**
 * 裁剪参数
 */
data class CropParams(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
    val rotation: Float = 0f,        // 旋转角度
    val scale: Float = 1f            // 缩放比例
)

/**
 * 人像信息（用于AI裁剪）
 */
data class PortraitInfo(
    val faceRectX: Float,
    val faceRectY: Float,
    val faceRectWidth: Float,
    val faceRectHeight: Float,
    val faceCenterX: Float,
    val faceCenterY: Float,
    val shoulderVisible: Boolean = false
)

/**
 * 图片处理接口 - KMP expect声明
 */
expect class ImageProcessor() {

    /**
     * 异步初始化处理器（加载模型等）
     */
    suspend fun initialize(): Boolean

    /**
     * 检查是否已初始化
     */
    fun isInitialized(): Boolean

    /**
     * 人像分割 - 去除背景
     * @param imageData 原始图片字节数组
     * @return 返回带透明通道的图片数据
     */
    suspend fun removeBackground(imageData: ByteArray): ProcessResult

    /**
     * 替换背景颜色
     * @param imageData 输入图片（可以是带透明通道的PNG）
     * @param backgroundColor 目标背景颜色
     * @return 返回替换背景后的图片数据
     */
    suspend fun replaceBackground(imageData: ByteArray, backgroundColor: BackgroundColor): ProcessResult

    /**
     * 智能抠图 + 背景替换（一步完成）
     * @param imageData 原始图片
     * @param backgroundColor 目标背景
     * @return 处理后的证件照
     */
    suspend fun process证件照(imageData: ByteArray, backgroundColor: BackgroundColor): ProcessResult

    /**
     * 图像美化调整
     * @param imageData 输入图片
     * @param params 美化参数
     * @return 调整后的图片
     */
    suspend fun enhance(imageData: ByteArray, params: EnhanceParams): ProcessResult

    /**
     * 裁剪图片
     * @param imageData 输入图片
     * @param params 裁剪参数
     * @return 裁剪后的图片
     */
    suspend fun crop(imageData: ByteArray, params: CropParams): ProcessResult

    /**
     * 按目标尺寸resize并保持比例
     * @param imageData 输入图片
     * @param targetWidth 目标宽度
     * @param targetHeight 目标高度
     * @param maintainAspectRatio 是否保持比例
     * @return 调整后的图片
     */
    suspend fun resize(imageData: ByteArray, targetWidth: Int, targetHeight: Int, maintainAspectRatio: Boolean = true): ProcessResult

    /**
     * 检测人像信息（用于AI裁剪）
     * @param imageData 输入图片
     * @return 人像信息
     */
    suspend fun detectPortrait(imageData: ByteArray): PortraitInfo?

    /**
     * 组合处理：抠图 + 背景替换 + 裁剪 + 美化
     * @param imageData 原始图片
     * @param backgroundColor 背景颜色
     * @param cropParams 裁剪参数（可选）
     * @param enhanceParams 美化参数（可选）
     * @return 最终证件照
     */
    suspend fun processComplete(
        imageData: ByteArray,
        backgroundColor: BackgroundColor,
        cropParams: CropParams? = null,
        enhanceParams: EnhanceParams? = null
    ): ProcessResult

    /**
     * 释放资源
     */
    fun release()
}

/**
 * 图片处理工具类
 */
object ImageProcessorUtil {

    /**
     * 将 Compose Color 转换为颜色值
     */
    fun Color.toColorInt(): Int = this.toArgb()

    /**
     * 将 BackgroundColor 转换为 Compose Color
     */
    fun BackgroundColor.toComposeColor(): Color = Color(this.colorValue)

    /**
     * 计算证件照最佳裁剪区域
     * 根据人像信息和目标比例
     */
    fun calculateBestCrop(
        portraitInfo: PortraitInfo,
        targetAspectRatio: Float,
        imageWidth: Int,
        imageHeight: Int
    ): CropParams {
        // 头部高度占照片高度60%左右
        val headHeightRatio = 0.6f

        // 人脸应居中，顶部留白8%-12%
        val topMarginRatio = 0.1f

        // 计算基于人脸的裁剪区域
        val faceCenterY = portraitInfo.faceCenterY

        // 计算照片高度（基于人脸高度）
        val photoHeight = (portraitInfo.faceRectHeight / headHeightRatio).toInt()
        val photoWidth = (photoHeight * targetAspectRatio).toInt()

        // 计算裁剪起始位置，使人脸居中
        var cropX = (portraitInfo.faceCenterX - photoWidth / 2).toInt()
        var cropY = (faceCenterY - photoHeight * (1 - topMarginRatio - 0.15f)).toInt()

        // 确保裁剪区域在图像范围内
        cropX = cropX.coerceIn(0, imageWidth - photoWidth)
        cropY = cropY.coerceIn(0, imageHeight - photoHeight)

        return CropParams(
            x = cropX,
            y = cropY,
            width = photoWidth,
            height = photoHeight,
            rotation = 0f,
            scale = 1f
        )
    }
}