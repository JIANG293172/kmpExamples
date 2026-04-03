package shared.imageprocessing

import shared.data.BackgroundColor

/**
 * iOS平台的图片处理器 - 简化版
 * 由于cinterop的复杂性，目前只是透传数据
 * 实际的图片处理需要使用原生iOS API（Core Image、Vision等）
 */
actual class ImageProcessor {

    actual suspend fun initialize(): Boolean = true

    actual fun isInitialized(): Boolean = true

    actual suspend fun removeBackground(imageData: ByteArray): ProcessResult {
        // iOS上需要使用Vision框架进行人像分割
        // 目前返回原图
        return ProcessResult(true, data = imageData)
    }

    actual suspend fun replaceBackground(imageData: ByteArray, backgroundColor: BackgroundColor): ProcessResult {
        // iOS上需要使用Core Image进行背景替换
        // 目前返回原图
        return ProcessResult(true, data = imageData)
    }

    actual suspend fun process证件照(imageData: ByteArray, backgroundColor: BackgroundColor): ProcessResult {
        return ProcessResult(true, data = imageData)
    }

    actual suspend fun enhance(imageData: ByteArray, params: EnhanceParams): ProcessResult {
        return ProcessResult(true, data = imageData)
    }

    actual suspend fun crop(imageData: ByteArray, params: CropParams): ProcessResult {
        return ProcessResult(true, data = imageData)
    }

    actual suspend fun resize(imageData: ByteArray, targetWidth: Int, targetHeight: Int, maintainAspectRatio: Boolean): ProcessResult {
        return ProcessResult(true, data = imageData)
    }

    actual suspend fun detectPortrait(imageData: ByteArray): PortraitInfo? {
        // 返回一个默认的人像信息
        return PortraitInfo(
            faceRectX = 0.25f,
            faceRectY = 0.1f,
            faceRectWidth = 0.5f,
            faceRectHeight = 0.4f,
            faceCenterX = 0.5f,
            faceCenterY = 0.3f,
            shoulderVisible = true
        )
    }

    actual suspend fun processComplete(
        imageData: ByteArray,
        backgroundColor: BackgroundColor,
        cropParams: CropParams?,
        enhanceParams: EnhanceParams?
    ): ProcessResult {
        return ProcessResult(true, data = imageData)
    }

    actual fun release() {
        // 无需释放的资源
    }
}