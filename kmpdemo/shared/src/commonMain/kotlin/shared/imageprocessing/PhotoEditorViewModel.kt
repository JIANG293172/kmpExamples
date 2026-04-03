package shared.imageprocessing

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import shared.data.BackgroundColor
import shared.data.BackgroundType

/**
 * 照片编辑状态
 */
data class PhotoEditorState(
    val originalImageData: ByteArray? = null,
    val processedImageData: ByteArray? = null,
    val isProcessing: Boolean = false,
    val selectedBackgroundColor: BackgroundColor = BackgroundColor.BLUE,
    val selectedBackgroundType: BackgroundType = BackgroundType.SOLID,
    val enhanceParams: EnhanceParams = EnhanceParams(),
    val cropParams: CropParams? = null,
    val errorMessage: String? = null,
    val isInitialized: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PhotoEditorState) return false
        if (originalImageData != null) {
            if (other.originalImageData == null) return false
            if (!originalImageData.contentEquals(other.originalImageData)) return false
        } else if (other.originalImageData != null) return false
        if (processedImageData != null) {
            if (other.processedImageData == null) return false
            if (!processedImageData.contentEquals(other.processedImageData)) return false
        } else if (other.processedImageData != null) return false
        if (isProcessing != other.isProcessing) return false
        if (selectedBackgroundColor != other.selectedBackgroundColor) return false
        if (selectedBackgroundType != other.selectedBackgroundType) return false
        if (enhanceParams != other.enhanceParams) return false
        if (cropParams != other.cropParams) return false
        if (errorMessage != other.errorMessage) return false
        if (isInitialized != other.isInitialized) return false
        return true
    }

    override fun hashCode(): Int {
        var result = originalImageData?.contentHashCode() ?: 0
        result = 31 * result + (processedImageData?.contentHashCode() ?: 0)
        result = 31 * result + isProcessing.hashCode()
        result = 31 * result + selectedBackgroundColor.hashCode()
        result = 31 * result + selectedBackgroundType.hashCode()
        result = 31 * result + enhanceParams.hashCode()
        result = 31 * result + (cropParams?.hashCode() ?: 0)
        result = 31 * result + (errorMessage?.hashCode() ?: 0)
        result = 31 * result + isInitialized.hashCode()
        return result
    }
}

/**
 * 照片编辑器ViewModel - 管理照片编辑状态和处理逻辑
 */
class PhotoEditorViewModel(
    private val processor: ImageProcessor = ImageProcessor()
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _state = MutableStateFlow(PhotoEditorState())
    val state: StateFlow<PhotoEditorState> = _state.asStateFlow()

    /**
     * 初始化图片处理器
     */
    suspend fun initialize() {
        val success = processor.initialize()
        _state.value = _state.value.copy(isInitialized = success)
    }

    /**
     * 设置原始图片
     */
    fun setOriginalImage(imageData: ByteArray) {
        _state.value = _state.value.copy(
            originalImageData = imageData,
            processedImageData = imageData
        )
    }

    /**
     * 设置背景颜色
     */
    fun setBackgroundColor(color: BackgroundColor) {
        _state.value = _state.value.copy(selectedBackgroundColor = color)
    }

    /**
     * 设置背景类型
     */
    fun setBackgroundType(type: BackgroundType) {
        _state.value = _state.value.copy(selectedBackgroundType = type)
    }

    /**
     * 更新美化参数
     */
    fun updateEnhanceParams(params: EnhanceParams) {
        _state.value = _state.value.copy(enhanceParams = params)
    }

    /**
     * 更新单个美化参数
     */
    fun updateBrightness(value: Float) {
        _state.value = _state.value.copy(
            enhanceParams = _state.value.enhanceParams.copy(brightness = value)
        )
    }

    fun updateContrast(value: Float) {
        _state.value = _state.value.copy(
            enhanceParams = _state.value.enhanceParams.copy(contrast = value)
        )
    }

    fun updateSharpness(value: Float) {
        _state.value = _state.value.copy(
            enhanceParams = _state.value.enhanceParams.copy(sharpness = value)
        )
    }

    fun updateDenoise(value: Float) {
        _state.value = _state.value.copy(
            enhanceParams = _state.value.enhanceParams.copy(denoise = value)
        )
    }

    fun updateExposure(value: Float) {
        _state.value = _state.value.copy(
            enhanceParams = _state.value.enhanceParams.copy(exposure = value)
        )
    }

    /**
     * 更新裁剪参数
     */
    fun updateCropParams(params: CropParams) {
        _state.value = _state.value.copy(cropParams = params)
    }

    /**
     * 重置美化参数
     */
    fun resetEnhanceParams() {
        _state.value = _state.value.copy(enhanceParams = EnhanceParams())
    }

    /**
     * 自动增强
     */
    fun autoEnhance() {
        _state.value = _state.value.copy(
            enhanceParams = EnhanceParams(
                brightness = 0.1f,
                contrast = 0.1f,
                sharpness = 0.2f,
                exposure = 0.05f
            )
        )
    }

    /**
     * 处理证件照 - 抠图 + 替换背景
     */
    fun process证件照() {
        val originalData = _state.value.originalImageData ?: return
        val bgColor = _state.value.selectedBackgroundColor

        _state.value = _state.value.copy(isProcessing = true, errorMessage = null)

        scope.launch {
            val result = processor.process证件照(originalData, bgColor)
            if (result.success && result.data != null) {
                _state.value = _state.value.copy(
                    processedImageData = result.data,
                    isProcessing = false
                )
            } else {
                _state.value = _state.value.copy(
                    isProcessing = false,
                    errorMessage = result.errorMessage ?: "处理失败"
                )
            }
        }
    }

    /**
     * 应用美化效果
     */
    fun applyEnhance() {
        val imageData = _state.value.processedImageData ?: return
        val params = _state.value.enhanceParams

        _state.value = _state.value.copy(isProcessing = true, errorMessage = null)

        scope.launch {
            val result = processor.enhance(imageData, params)
            if (result.success && result.data != null) {
                _state.value = _state.value.copy(
                    processedImageData = result.data,
                    isProcessing = false
                )
            } else {
                _state.value = _state.value.copy(
                    isProcessing = false,
                    errorMessage = result.errorMessage ?: "美化处理失败"
                )
            }
        }
    }

    /**
     * 应用裁剪
     */
    fun applyCrop() {
        val imageData = _state.value.processedImageData ?: return
        val cropParams = _state.value.cropParams ?: return

        _state.value = _state.value.copy(isProcessing = true, errorMessage = null)

        scope.launch {
            val result = processor.crop(imageData, cropParams)
            if (result.success && result.data != null) {
                _state.value = _state.value.copy(
                    processedImageData = result.data,
                    isProcessing = false,
                    cropParams = null
                )
            } else {
                _state.value = _state.value.copy(
                    isProcessing = false,
                    errorMessage = result.errorMessage ?: "裁剪失败"
                )
            }
        }
    }

    /**
     * 检测人像信息（用于AI裁剪）
     */
    suspend fun detectPortrait(): PortraitInfo? {
        val imageData = _state.value.originalImageData ?: return null
        return processor.detectPortrait(imageData)
    }

    /**
     * 完整处理流程：抠图 + 背景 + 裁剪 + 美化
     */
    fun processComplete() {
        val originalData = _state.value.originalImageData ?: return
        val bgColor = _state.value.selectedBackgroundColor
        val cropParams = _state.value.cropParams
        val enhanceParams = _state.value.enhanceParams

        _state.value = _state.value.copy(isProcessing = true, errorMessage = null)

        scope.launch {
            val result = processor.processComplete(
                originalData,
                bgColor,
                cropParams,
                enhanceParams
            )
            if (result.success && result.data != null) {
                _state.value = _state.value.copy(
                    processedImageData = result.data,
                    isProcessing = false,
                    cropParams = null
                )
            } else {
                _state.value = _state.value.copy(
                    isProcessing = false,
                    errorMessage = result.errorMessage ?: "处理失败"
                )
            }
        }
    }

    /**
     * 获取当前处理的图片数据
     */
    fun getProcessedImageData(): ByteArray? = _state.value.processedImageData

    /**
     * 清除错误信息
     */
    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    /**
     * 释放资源
     */
    fun release() {
        processor.release()
    }
}