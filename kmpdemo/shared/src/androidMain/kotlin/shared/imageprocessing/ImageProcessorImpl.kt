package shared.imageprocessing

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import shared.data.BackgroundColor
import kotlin.math.max
import kotlin.math.min

actual class ImageProcessor {
    actual suspend fun initialize(): Boolean = true

    actual fun isInitialized(): Boolean = true

    private suspend fun loadBitmap(imageData: ByteArray): Bitmap? = withContext(Dispatchers.IO) {
        try {
            BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun bitmapToByteArray(bitmap: Bitmap, format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG): ByteArray {
        val stream = java.io.ByteArrayOutputStream()
        bitmap.compress(format, 100, stream)
        return stream.toByteArray()
    }

    /**
     * 基于颜色差异的背景去除
     * 通过检测与角落颜色的差异来分离主体
     */
    actual suspend fun removeBackground(imageData: ByteArray): ProcessResult = withContext(Dispatchers.IO) {
        val originalBitmap = loadBitmap(imageData) ?: return@withContext ProcessResult(false, errorMessage = "无法加载图片")

        try {
            val width = originalBitmap.width
            val height = originalBitmap.height

            // 获取四个角的颜色作为背景参考
            val cornerColors = listOf(
                originalBitmap.getPixel(0, 0),
                originalBitmap.getPixel(width - 1, 0),
                originalBitmap.getPixel(0, height - 1),
                originalBitmap.getPixel(width - 1, height - 1)
            )

            // 计算平均背景色
            val avgR = cornerColors.map { Color.red(it) }.average().toInt()
            val avgG = cornerColors.map { Color.green(it) }.average().toInt()
            val avgB = cornerColors.map { Color.blue(it) }.average().toInt()
            val backgroundColor = Color.rgb(avgR, avgG, avgB)

            // 创建带透明通道的结果图
            val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(result)

            // 绘制原图
            canvas.drawBitmap(originalBitmap, 0f, 0f, null)

            // 设置混合模式为去除背景
            val paint = Paint().apply {
                xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
            }

            // 创建mask图
            val mask = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val threshold = 60 // 颜色差异阈值

            for (y in 0 until height) {
                for (x in 0 until width) {
                    val pixel = originalBitmap.getPixel(x, y)
                    val diff = colorDifference(pixel, backgroundColor)

                    if (diff < threshold) {
                        mask.setPixel(x, y, Color.TRANSPARENT)
                    } else {
                        // 边缘部分渐变透明
                        val alpha = ((diff - threshold) * 255 / threshold).coerceIn(0, 255)
                        mask.setPixel(x, y, Color.argb(alpha, 255, 255, 255))
                    }
                }
            }

            // 应用mask
            canvas.drawBitmap(mask, 0f, 0f, paint)
            mask.recycle()
            originalBitmap.recycle()

            ProcessResult(true, data = bitmapToByteArray(result))
        } catch (e: Exception) {
            ProcessResult(false, errorMessage = "背景去除失败: ${e.message}")
        }
    }

    private fun colorDifference(c1: Int, c2: Int): Int {
        val rDiff = Color.red(c1) - Color.red(c2)
        val gDiff = Color.green(c1) - Color.green(c2)
        val bDiff = Color.blue(c1) - Color.blue(c2)
        return kotlin.math.sqrt((rDiff * rDiff + gDiff * gDiff + bDiff * bDiff).toDouble()).toInt()
    }

    actual suspend fun replaceBackground(imageData: ByteArray, backgroundColor: BackgroundColor): ProcessResult = withContext(Dispatchers.IO) {
        val bitmap = loadBitmap(imageData) ?: return@withContext ProcessResult(false, errorMessage = "无法加载图片")

        try {
            val width = bitmap.width
            val height = bitmap.height

            val targetColor = when (backgroundColor) {
                BackgroundColor.WHITE -> Color.WHITE
                BackgroundColor.RED -> Color.RED
                BackgroundColor.BLUE -> Color.rgb(0, 153, 255)
                BackgroundColor.LIGHT_BLUE -> Color.rgb(102, 204, 255)
                BackgroundColor.LIGHT_GRAY -> Color.rgb(230, 230, 230)
                BackgroundColor.DARK_RED -> Color.rgb(204, 0, 0)
                BackgroundColor.DARK_BLUE -> Color.rgb(0, 51, 153)
                BackgroundColor.PALE_BLUE -> Color.rgb(189, 215, 238)
                BackgroundColor.TRANSPARENT -> Color.TRANSPARENT
                else -> Color.WHITE
            }

            val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(result)

            // 填充背景色
            canvas.drawColor(targetColor)

            // 绘制原图
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            bitmap.recycle()

            ProcessResult(true, data = bitmapToByteArray(result))
        } catch (e: Exception) {
            ProcessResult(false, errorMessage = "背景替换失败: ${e.message}")
        }
    }

    actual suspend fun process证件照(imageData: ByteArray, backgroundColor: BackgroundColor): ProcessResult = withContext(Dispatchers.IO) {
        // 步骤1: 抠图
        val noBgResult = removeBackground(imageData)
        if (!noBgResult.success || noBgResult.data == null) {
            return@withContext noBgResult
        }

        // 步骤2: 替换背景
        replaceBackground(noBgResult.data, backgroundColor)
    }

    actual suspend fun enhance(imageData: ByteArray, params: EnhanceParams): ProcessResult = withContext(Dispatchers.IO) {
        val bitmap = loadBitmap(imageData) ?: return@withContext ProcessResult(false, errorMessage = "无法加载图片")

        try {
            val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(result)

            val paint = Paint()

            // 构建颜色矩阵
            val colorMatrix = ColorMatrix()

            // 曝光调整
            if (params.exposure != 0f) {
                val exposureScale = 1f + params.exposure
                colorMatrix.setScale(exposureScale, exposureScale, exposureScale, 1f)
            }

            // 亮度调整
            if (params.brightness != 0f) {
                val brightness = params.brightness * 255
                val brightnessMatrix = ColorMatrix(floatArrayOf(
                    1f, 0f, 0f, 0f, brightness,
                    0f, 1f, 0f, 0f, brightness,
                    0f, 0f, 1f, 0f, brightness,
                    0f, 0f, 0f, 1f, 0f
                ))
                colorMatrix.postConcat(brightnessMatrix)
            }

            // 对比度调整
            if (params.contrast != 0f) {
                val contrast = 1f + params.contrast
                val translate = (-0.5f * contrast + 0.5f) * 255
                val contrastMatrix = ColorMatrix(floatArrayOf(
                    contrast, 0f, 0f, 0f, translate,
                    0f, contrast, 0f, 0f, translate,
                    0f, 0f, contrast, 0f, translate,
                    0f, 0f, 0f, 1f, 0f
                ))
                colorMatrix.postConcat(contrastMatrix)
            }

            paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
            canvas.drawBitmap(bitmap, 0f, 0f, paint)

            bitmap.recycle()
            ProcessResult(true, data = bitmapToByteArray(result))
        } catch (e: Exception) {
            ProcessResult(false, errorMessage = "美化处理失败: ${e.message}")
        }
    }

    actual suspend fun crop(imageData: ByteArray, params: CropParams): ProcessResult = withContext(Dispatchers.IO) {
        val bitmap = loadBitmap(imageData) ?: return@withContext ProcessResult(false, errorMessage = "无法加载图片")

        try {
            var workingBitmap = bitmap

            // 应用旋转变换
            if (params.rotation != 0f) {
                val matrix = Matrix()
                matrix.postRotate(params.rotation)
                val rotated = Bitmap.createBitmap(
                    workingBitmap, 0, 0,
                    workingBitmap.width, workingBitmap.height,
                    matrix, true
                )
                if (workingBitmap != bitmap) {
                    workingBitmap.recycle()
                }
                workingBitmap = rotated
            }

            // 应用缩放
            if (params.scale != 1f) {
                val newWidth = (workingBitmap.width * params.scale).toInt().coerceAtLeast(1)
                val newHeight = (workingBitmap.height * params.scale).toInt().coerceAtLeast(1)
                val scaled = Bitmap.createScaledBitmap(workingBitmap, newWidth, newHeight, true)
                if (workingBitmap != bitmap) {
                    workingBitmap.recycle()
                }
                workingBitmap = scaled
            }

            // 确保裁剪区域在范围内
            val cropX = params.x.coerceIn(0, max(0, workingBitmap.width - 1))
            val cropY = params.y.coerceIn(0, max(0, workingBitmap.height - 1))
            val cropWidth = min(params.width.coerceIn(1, workingBitmap.width - cropX), workingBitmap.width - cropX)
            val cropHeight = min(params.height.coerceIn(1, workingBitmap.height - cropY), workingBitmap.height - cropY)

            val cropped = if (cropWidth > 0 && cropHeight > 0) {
                Bitmap.createBitmap(workingBitmap, cropX, cropY, cropWidth, cropHeight)
            } else {
                workingBitmap
            }

            if (workingBitmap != bitmap && workingBitmap != cropped) {
                workingBitmap.recycle()
            }
            bitmap.recycle()

            ProcessResult(true, data = bitmapToByteArray(cropped))
        } catch (e: Exception) {
            ProcessResult(false, errorMessage = "裁剪失败: ${e.message}")
        }
    }

    actual suspend fun resize(imageData: ByteArray, targetWidth: Int, targetHeight: Int, maintainAspectRatio: Boolean): ProcessResult = withContext(Dispatchers.IO) {
        val bitmap = loadBitmap(imageData) ?: return@withContext ProcessResult(false, errorMessage = "无法加载图片")

        try {
            val (newWidth, newHeight) = if (maintainAspectRatio) {
                val ratio = minOf(
                    targetWidth.toFloat() / bitmap.width,
                    targetHeight.toFloat() / bitmap.height
                )
                Pair(
                    (bitmap.width * ratio).toInt().coerceAtLeast(1),
                    (bitmap.height * ratio).toInt().coerceAtLeast(1)
                )
            } else {
                Pair(targetWidth.coerceAtLeast(1), targetHeight.coerceAtLeast(1))
            }

            val resized = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
            bitmap.recycle()
            ProcessResult(true, data = bitmapToByteArray(resized))
        } catch (e: Exception) {
            ProcessResult(false, errorMessage = "缩放失败: ${e.message}")
        }
    }

    actual suspend fun detectPortrait(imageData: ByteArray): PortraitInfo? = withContext(Dispatchers.IO) {
        val bitmap = loadBitmap(imageData) ?: return@withContext null

        try {
            val width = bitmap.width
            val height = bitmap.height

            // 简化实现：假设人脸在图像中心偏上区域
            bitmap.recycle()
            PortraitInfo(
                faceRectX = width * 0.25f,
                faceRectY = height * 0.1f,
                faceRectWidth = width * 0.5f,
                faceRectHeight = height * 0.4f,
                faceCenterX = width * 0.5f,
                faceCenterY = height * 0.3f,
                shoulderVisible = true
            )
        } catch (e: Exception) {
            null
        }
    }

    actual suspend fun processComplete(
        imageData: ByteArray,
        backgroundColor: BackgroundColor,
        cropParams: CropParams?,
        enhanceParams: EnhanceParams?
    ): ProcessResult = withContext(Dispatchers.IO) {
        var currentData = imageData

        // 1. 抠图 + 替换背景
        val photoResult = process证件照(currentData, backgroundColor)
        if (!photoResult.success || photoResult.data == null) {
            return@withContext photoResult
        }
        currentData = photoResult.data

        // 2. 裁剪
        if (cropParams != null) {
            val cropResult = crop(currentData, cropParams)
            if (cropResult.success && cropResult.data != null) {
                currentData = cropResult.data
            }
        }

        // 3. 美化
        if (enhanceParams != null) {
            val enhanceResult = enhance(currentData, enhanceParams)
            if (enhanceResult.success && enhanceResult.data != null) {
                currentData = enhanceResult.data
            }
        }

        ProcessResult(true, data = currentData)
    }

    actual fun release() {
        // 无需释放的资源
    }
}