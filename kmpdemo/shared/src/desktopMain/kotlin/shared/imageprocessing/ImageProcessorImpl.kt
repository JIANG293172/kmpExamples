package shared.imageprocessing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import shared.data.BackgroundColor
import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

actual class ImageProcessor {
    actual suspend fun initialize(): Boolean = true

    actual fun isInitialized(): Boolean = true

    private fun byteArrayToBufferedImage(data: ByteArray): BufferedImage? {
        return try {
            ImageIO.read(ByteArrayInputStream(data))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun bufferedImageToByteArray(image: BufferedImage, format: String = "PNG"): ByteArray {
        return try {
            val stream = ByteArrayOutputStream()
            ImageIO.write(image, format, stream)
            stream.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            ByteArray(0)
        }
    }

    /**
     * 基于颜色检测的简单背景去除
     * 对于证件照，通常背景是单色的（白、蓝、红等）
     * 这个方法通过检测边缘和颜色差异来尝试分离主体
     */
    actual suspend fun removeBackground(imageData: ByteArray): ProcessResult = withContext(Dispatchers.IO) {
        val originalImage = byteArrayToBufferedImage(imageData)
            ?: return@withContext ProcessResult(false, errorMessage = "无法加载图片")

        try {
            val width = originalImage.width
            val height = originalImage.height

            // 创建输出图像（带透明通道）
            val result = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
            val graphics = result.createGraphics()

            // 绘制原图
            graphics.drawImage(originalImage, 0, 0, null)

            // 获取图像的角颜色作为背景参考（通常背景色接近角落颜色）
            val cornerColors = listOf(
                Color(originalImage.getRGB(0, 0)),
                Color(originalImage.getRGB(width - 1, 0)),
                Color(originalImage.getRGB(0, height - 1)),
                Color(originalImage.getRGB(width - 1, height - 1))
            )

            // 取平均作为背景色
            val avgR = cornerColors.map { it.red.toInt() }.average().toInt()
            val avgG = cornerColors.map { it.green.toInt() }.average().toInt()
            val avgB = cornerColors.map { it.blue.toInt() }.average().toInt()
            val backgroundColor = Color(avgR, avgG, avgB)

            // 遍历每个像素，检测是否接近背景色
            val threshold = 60 // 颜色差异阈值

            for (y in 0 until height) {
                for (x in 0 until width) {
                    val pixel = Color(originalImage.getRGB(x, y))
                    val diff = colorDifference(pixel, backgroundColor)

                    if (diff < threshold) {
                        // 接近背景色，设置透明
                        result.setRGB(x, y, 0)
                    }
                }
            }

            graphics.dispose()
            ProcessResult(true, data = bufferedImageToByteArray(result))
        } catch (e: Exception) {
            ProcessResult(false, errorMessage = "背景去除失败: ${e.message}")
        }
    }

    private fun colorDifference(c1: Color, c2: Color): Int {
        val rDiff = (c1.red - c2.red).toInt()
        val gDiff = (c1.green - c2.green).toInt()
        val bDiff = (c1.blue - c2.blue).toInt()
        return Math.sqrt((rDiff * rDiff + gDiff * gDiff + bDiff * bDiff).toDouble()).toInt()
    }

    actual suspend fun replaceBackground(imageData: ByteArray, backgroundColor: BackgroundColor): ProcessResult = withContext(Dispatchers.IO) {
        val inputImage = byteArrayToBufferedImage(imageData)
            ?: return@withContext ProcessResult(false, errorMessage = "无法加载图片")

        try {
            val width = inputImage.width
            val height = inputImage.height

            // 创建结果图像
            val result = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
            val graphics = result.createGraphics()

            // 填充背景色
            val bgColor = when (backgroundColor) {
                BackgroundColor.WHITE -> Color.WHITE
                BackgroundColor.RED -> Color.RED
                BackgroundColor.BLUE -> Color(0, 153, 255)
                BackgroundColor.LIGHT_BLUE -> Color(102, 204, 255)
                BackgroundColor.LIGHT_GRAY -> Color(230, 230, 230)
                BackgroundColor.DARK_RED -> Color(204, 0, 0)
                BackgroundColor.DARK_BLUE -> Color(0, 51, 153)
                BackgroundColor.PALE_BLUE -> Color(189, 215, 238)
                BackgroundColor.TRANSPARENT -> Color.WHITE // 透明用白色代替
                else -> Color.WHITE
            }

            graphics.color = bgColor
            graphics.fillRect(0, 0, width, height)

            // 如果输入图像有透明通道，处理透明像素
            if (inputImage.transparency == BufferedImage.TRANSLUCENT) {
                graphics.drawImage(inputImage, 0, 0, null)
            } else {
                // 原图直接绘制
                graphics.drawImage(inputImage, 0, 0, null)
            }

            graphics.dispose()
            ProcessResult(true, data = bufferedImageToByteArray(result, "PNG"))
        } catch (e: Exception) {
            ProcessResult(false, errorMessage = "背景替换失败: ${e.message}")
        }
    }

    actual suspend fun process证件照(imageData: ByteArray, backgroundColor: BackgroundColor): ProcessResult = withContext(Dispatchers.IO) {
        // 1. 抠图
        val noBgResult = removeBackground(imageData)
        if (!noBgResult.success || noBgResult.data == null) {
            return@withContext noBgResult
        }

        // 2. 替换背景
        replaceBackground(noBgResult.data, backgroundColor)
    }

    actual suspend fun enhance(imageData: ByteArray, params: EnhanceParams): ProcessResult = withContext(Dispatchers.IO) {
        val inputImage = byteArrayToBufferedImage(imageData)
            ?: return@withContext ProcessResult(false, errorMessage = "无法加载图片")

        try {
            // 创建结果图像
            val result = BufferedImage(inputImage.width, inputImage.height, BufferedImage.TYPE_INT_RGB)
            val graphics = result.createGraphics()

            // 设置渲染质量
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

            // 应用颜色变换
            val colorMatrix = FloatArray(4 * 5)

            // 基础变换矩阵（单位矩阵）
            colorMatrix[0] = 1f; colorMatrix[6] = 1f; colorMatrix[12] = 1f; colorMatrix[18] = 1f

            // 曝光
            if (params.exposure != 0f) {
                val exposureScale = 1f + params.exposure
                colorMatrix[0] *= exposureScale
                colorMatrix[6] *= exposureScale
                colorMatrix[12] *= exposureScale
            }

            // 亮度
            if (params.brightness != 0f) {
                val brightness = params.brightness * 255
                colorMatrix[4] = brightness
                colorMatrix[9] = brightness
                colorMatrix[14] = brightness
            }

            // 对比度
            if (params.contrast != 0f) {
                val contrast = 1f + params.contrast
                val translate = (-0.5f * contrast + 0.5f) * 255
                colorMatrix[0] *= contrast; colorMatrix[4] += translate
                colorMatrix[6] *= contrast; colorMatrix[9] += translate
                colorMatrix[12] *= contrast; colorMatrix[14] += translate
            }

            // 应用滤镜
            val colorConvert = java.awt.image.ColorConvertOp(java.awt.color.ColorSpace.getInstance(java.awt.color.ColorSpace.CS_sRGB), graphics.renderingHints)

            // 简化处理：直接调整像素
            for (y in 0 until inputImage.height) {
                for (x in 0 until inputImage.width) {
                    val pixel = Color(inputImage.getRGB(x, y))

                    var r = pixel.red / 255f
                    var g = pixel.green / 255f
                    var b = pixel.blue / 255f

                    // 应用曝光
                    if (params.exposure != 0f) {
                        val exposureScale = 1f + params.exposure
                        r *= exposureScale
                        g *= exposureScale
                        b *= exposureScale
                    }

                    // 应用亮度
                    if (params.brightness != 0f) {
                        r += params.brightness
                        g += params.brightness
                        b += params.brightness
                    }

                    // 应用对比度
                    if (params.contrast != 0f) {
                        val contrast = 1f + params.contrast
                        r = r * contrast + (-0.5f * contrast + 0.5f)
                        g = g * contrast + (-0.5f * contrast + 0.5f)
                        b = b * contrast + (-0.5f * contrast + 0.5f)
                    }

                    // 裁剪到有效范围
                    r = r.coerceIn(0f, 1f)
                    g = g.coerceIn(0f, 1f)
                    b = b.coerceIn(0f, 1f)

                    result.setRGB(x, y, Color(r, g, b).rgb)
                }
            }

            // 降噪（简单的高斯模糊降采样）
            if (params.denoise > 0f) {
                val denoised = applySimpleDenoise(result, params.denoise)
                return@withContext ProcessResult(true, data = bufferedImageToByteArray(denoised))
            }

            graphics.dispose()
            ProcessResult(true, data = bufferedImageToByteArray(result))
        } catch (e: Exception) {
            ProcessResult(false, errorMessage = "美化处理失败: ${e.message}")
        }
    }

    private fun applySimpleDenoise(image: BufferedImage, amount: Float): BufferedImage {
        val factor = (1f - amount * 0.5f).coerceIn(0.5f, 1f)
        val newWidth = (image.width * factor).toInt().coerceAtLeast(1)
        val newHeight = (image.height * factor).toInt().coerceAtLeast(1)

        val scaled = BufferedImage(newWidth, newHeight, image.type)
        val graphics = scaled.createGraphics()
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        graphics.drawImage(image, 0, 0, newWidth, newHeight, null)
        graphics.dispose()

        val result = BufferedImage(image.width, image.height, image.type)
        val resultGraphics = result.createGraphics()
        resultGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        resultGraphics.drawImage(scaled, 0, 0, image.width, image.height, null)
        resultGraphics.dispose()
        scaled.flush()

        return result
    }

    actual suspend fun crop(imageData: ByteArray, params: CropParams): ProcessResult = withContext(Dispatchers.IO) {
        val inputImage = byteArrayToBufferedImage(imageData)
            ?: return@withContext ProcessResult(false, errorMessage = "无法加载图片")

        try {
            var result = inputImage

            // 应用旋转变换
            if (params.rotation != 0f) {
                val angle = Math.toRadians(params.rotation.toDouble())
                val cos = Math.cos(angle).toFloat()
                val sin = Math.sin(angle).toFloat()

                val rotatedWidth = (inputImage.width * Math.abs(cos) + inputImage.height * Math.abs(sin)).toInt()
                val rotatedHeight = (inputImage.width * Math.abs(sin) + inputImage.height * Math.abs(cos)).toInt()

                val rotated = BufferedImage(rotatedWidth, rotatedHeight, inputImage.type)
                val graphics = rotated.createGraphics()
                graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)

                val transform = AffineTransform()
                transform.translate(rotatedWidth / 2.0, rotatedHeight / 2.0)
                transform.rotate(angle.toDouble())
                transform.translate(-inputImage.width / 2.0, -inputImage.height / 2.0)

                val op = AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR)
                result = op.filter(inputImage, rotated)
                graphics.dispose()
                inputImage.flush()
            }

            // 应用缩放
            if (params.scale != 1f) {
                val newWidth = (result.width * params.scale).toInt()
                val newHeight = (result.height * params.scale).toInt()
                val scaled = BufferedImage(newWidth, newHeight, result.type)
                val graphics = scaled.createGraphics()
                graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
                graphics.drawImage(result, 0, 0, newWidth, newHeight, null)
                graphics.dispose()
                if (result != inputImage) result.flush()
                result = scaled
            }

            // 裁剪
            val cropX = params.x.coerceIn(0, result.width - 1)
            val cropY = params.y.coerceIn(0, result.height - 1)
            val cropWidth = params.width.coerceIn(1, result.width - cropX)
            val cropHeight = params.height.coerceIn(1, result.height - cropY)

            val cropped = result.getSubimage(cropX, cropY, cropWidth, cropHeight)

            if (result != inputImage) result.flush()

            ProcessResult(true, data = bufferedImageToByteArray(cropped))
        } catch (e: Exception) {
            ProcessResult(false, errorMessage = "裁剪失败: ${e.message}")
        }
    }

    actual suspend fun resize(imageData: ByteArray, targetWidth: Int, targetHeight: Int, maintainAspectRatio: Boolean): ProcessResult = withContext(Dispatchers.IO) {
        val inputImage = byteArrayToBufferedImage(imageData)
            ?: return@withContext ProcessResult(false, errorMessage = "无法加载图片")

        try {
            val (newWidth, newHeight) = if (maintainAspectRatio) {
                val ratio = minOf(
                    targetWidth.toDouble() / inputImage.width,
                    targetHeight.toDouble() / inputImage.height
                )
                Pair(
                    (inputImage.width * ratio).toInt(),
                    (inputImage.height * ratio).toInt()
                )
            } else {
                Pair(targetWidth, targetHeight)
            }

            val resized = BufferedImage(newWidth, newHeight, inputImage.type)
            val graphics = resized.createGraphics()
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
            graphics.drawImage(inputImage, 0, 0, newWidth, newHeight, null)
            graphics.dispose()
            inputImage.flush()

            ProcessResult(true, data = bufferedImageToByteArray(resized))
        } catch (e: Exception) {
            ProcessResult(false, errorMessage = "缩放失败: ${e.message}")
        }
    }

    actual suspend fun detectPortrait(imageData: ByteArray): PortraitInfo? = withContext(Dispatchers.IO) {
        val image = byteArrayToBufferedImage(imageData) ?: return@withContext null

        try {
            // 简化实现：假设人脸在图像中心偏上区域
            val width = image.width
            val height = image.height

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
        val证件照Result = process证件照(currentData, backgroundColor)
        if (!证件照Result.success ||证件照Result.data == null) {
            return@withContext证件照Result
        }
        currentData =证件照Result.data

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