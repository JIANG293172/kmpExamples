package shared.ui.screens.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import shared.data.CropMode
import shared.data.CropRatios
import shared.data.PhotoSize
import shared.navigation.AppState
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

private fun Float.formatString(decimals: Int): String {
    val factor = 10.0.pow(decimals).toInt()
    val scaled = (this * factor).roundToInt()
    val whole = scaled / factor
    val frac = scaled % factor
    return "$whole.${frac.toString().padStart(decimals, '0')}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropScreen(
    state: AppState,
    onBack: () -> Unit,
    onDone: (CropResult) -> Unit
) {
    val photoSize = state.selectedPhotoSize

    var cropMode by remember { mutableStateOf(CropMode.AI_AUTO) }
    var rotation by remember { mutableFloatStateOf(0f) }  // 旋转角度 -15° ~ +15°
    var scale by remember { mutableFloatStateOf(1f) }     // 缩放 0.5x ~ 2.0x
    var offset by remember { mutableStateOf(Offset.Zero) } // 拖动偏移

    // 计算裁剪比例
    val aspectRatio = photoSize?.let {
        CropRatios.getRatioForSize(it.id)
    } ?: CropRatios.ONE_INCH

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("裁剪照片") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        // 重置所有调整
                        rotation = 0f
                        scale = 1f
                        offset = Offset.Zero
                    }) {
                        Text("重置")
                    }
                    TextButton(onClick = {
                        onDone(CropResult(
                            rotation = rotation,
                            scale = scale,
                            offset = offset,
                            mode = cropMode,
                            aspectRatio = aspectRatio
                        ))
                    }) {
                        Text("完成", color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 裁剪预览区域
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                CropPreview(
                    rotation = rotation,
                    scale = scale,
                    offset = offset,
                    aspectRatio = aspectRatio,
                    cropMode = cropMode,
                    onTransform = { newScale, newOffset, newRotation ->
                        // 限制范围
                        scale = newScale.coerceIn(0.5f, 2.0f)
                        offset = newOffset
                        rotation = newRotation.coerceIn(-15f, 15f)
                    }
                )
            }

            // 裁剪工具面板
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.4f),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 裁剪模式选择
                    TabRow(
                        selectedTabIndex = cropMode.ordinal
                    ) {
                        Tab(
                            selected = cropMode == CropMode.AI_AUTO,
                            onClick = { cropMode = CropMode.AI_AUTO },
                            text = { Text("AI智能") },
                            icon = { Icon(Icons.Default.AutoAwesome, null) }
                        )
                        Tab(
                            selected = cropMode == CropMode.RATIO_LOCKED,
                            onClick = { cropMode = CropMode.RATIO_LOCKED },
                            text = { Text("比例锁定") },
                            icon = { Icon(Icons.Default.Lock, null) }
                        )
                        Tab(
                            selected = cropMode == CropMode.FREE,
                            onClick = { cropMode = CropMode.FREE },
                            text = { Text("自由裁剪") },
                            icon = { Icon(Icons.Default.CropFree, null) }
                        )
                    }

                    // 裁剪内容
                    when (cropMode) {
                        CropMode.AI_AUTO -> AIAutoCropTab(
                            photoSize = photoSize,
                            rotation = rotation,
                            onRotationChange = { rotation = it }
                        )
                        CropMode.RATIO_LOCKED -> RatioLockedTab(
                            aspectRatio = aspectRatio,
                            photoSize = photoSize,
                            rotation = rotation,
                            scale = scale,
                            onRotationChange = { rotation = it },
                            onScaleChange = { scale = it }
                        )
                        CropMode.FREE -> FreeCropTab(
                            rotation = rotation,
                            scale = scale,
                            onRotationChange = { rotation = it },
                            onScaleChange = { scale = it }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CropPreview(
    rotation: Float,
    scale: Float,
    offset: Offset,
    aspectRatio: Float,
    cropMode: CropMode,
    onTransform: (Float, Offset, Float) -> Unit
) {
    var currentScale by remember { mutableFloatStateOf(scale) }
    var currentOffset by remember { mutableStateOf(offset) }
    var currentRotation by remember { mutableFloatStateOf(rotation) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        // 照片区域（模拟）
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .aspectRatio(1f / aspectRatio)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFE0E0E0),
                            Color(0xFFBDBDBD)
                        )
                    )
                )
                .pointerInput(Unit) {
                    detectTransformGestures { centroid, pan, zoom, rotationDelta ->
                        currentScale = (currentScale * zoom).coerceIn(0.5f, 2.0f)
                        currentOffset = currentOffset + pan
                        currentRotation = (currentRotation + rotationDelta).coerceIn(-15f, 15f)
                        onTransform(currentScale, currentOffset, currentRotation)
                    }
                }
        ) {
            // 人脸占位符
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Face,
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                    tint = Color.Gray
                )
            }
        }

        // 裁剪框
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // 计算裁剪框尺寸（保持比例）
            val cropWidth = canvasWidth * 0.7f
            val cropHeight = cropWidth / aspectRatio

            val left = (canvasWidth - cropWidth) / 2
            val top = (canvasHeight - cropHeight) / 2

            // 绘制半透明遮罩
            val maskPath = Path().apply {
                addRect(Rect(0f, 0f, canvasWidth, canvasHeight))
                addRect(Rect(left, top, left + cropWidth, top + cropHeight))
            }
            clipPath(maskPath) {
                drawRect(Color.Black.copy(alpha = 0.5f))
            }

            // 绘制裁剪边框
            drawRect(
                color = Color.White,
                topLeft = Offset(left, top),
                size = Size(cropWidth, cropHeight),
                style = Stroke(width = 2f)
            )

            // 绘制网格线
            val gridColor = Color.White.copy(alpha = 0.5f)
            val gridStroke = Stroke(width = 1f)

            // 横线
            for (i in 1..2) {
                val y = top + (cropHeight / 3) * i
                drawLine(
                    color = gridColor,
                    start = Offset(left, y),
                    end = Offset(left + cropWidth, y),
                    strokeWidth = gridStroke.width
                )
            }

            // 竖线
            for (i in 1..2) {
                val x = left + (cropWidth / 3) * i
                drawLine(
                    color = gridColor,
                    start = Offset(x, top),
                    end = Offset(x, top + cropHeight),
                    strokeWidth = gridStroke.width
                )
            }

            // 绘制角落手柄
            val handleSize = 12f
            val handleColor = Color.White

            // 四角
            listOf(
                Offset(left, top),
                Offset(left + cropWidth, top),
                Offset(left, top + cropHeight),
                Offset(left + cropWidth, top + cropHeight)
            ).forEach { corner ->
                drawCircle(
                    color = handleColor,
                    radius = handleSize,
                    center = corner
                )
            }
        }
    }
}

@Composable
private fun AIAutoCropTab(
    photoSize: PhotoSize?,
    rotation: Float,
    onRotationChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // AI说明
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "AI智能裁剪",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "自动检测人脸和肩部，智能构图居中",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 旋转调整
        Text(
            text = "微调旋转",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.RotateLeft,
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .pointerInput(Unit) {
                        detectTapGestures {
                            onRotationChange((rotation - 1f).coerceIn(-15f, 15f))
                        }
                    }
            )

            Slider(
                value = rotation,
                onValueChange = onRotationChange,
                valueRange = -15f..15f,
                steps = 29,
                modifier = Modifier.weight(1f)
            )

            Icon(
                Icons.Default.RotateRight,
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .pointerInput(Unit) {
                        detectTapGestures {
                            onRotationChange((rotation + 1f).coerceIn(-15f, 15f))
                        }
                    }
            )

            Text(
                text = "${rotation.toInt()}°",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.width(40.dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // AI裁剪规则说明
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = "AI裁剪规则",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                CropRuleItem("头部高度占照片高度：60%±5%")
                CropRuleItem("顶部留白：8%~12%")
                CropRuleItem("肩部完整露出，不裁切")
                CropRuleItem("人脸垂直居中，视线水平")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 尺寸信息
        photoSize?.let { size ->
            Text(
                text = "目标尺寸: ${size.widthMm}×${size.heightMm}mm (${size.widthPx}×${size.heightPx}px)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun RatioLockedTab(
    aspectRatio: Float,
    photoSize: PhotoSize?,
    rotation: Float,
    scale: Float,
    onRotationChange: (Float) -> Unit,
    onScaleChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 比例锁定说明
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "比例锁定",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "当前比例: ${aspectRatio.formatString(2)}:1",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 缩放控制
        Text(
            text = "缩放调整",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "0.5x", style = MaterialTheme.typography.bodySmall)
            Slider(
                value = scale,
                onValueChange = onScaleChange,
                valueRange = 0.5f..2.0f,
                modifier = Modifier.weight(1f)
            )
            Text(text = "2.0x", style = MaterialTheme.typography.bodySmall)
            Text(
                text = "${scale.formatString(1)}x",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.width(40.dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 旋转控制
        Text(
            text = "旋转调整",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.RotateLeft,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            Slider(
                value = rotation,
                onValueChange = onRotationChange,
                valueRange = -15f..15f,
                steps = 29,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Default.RotateRight,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "${rotation.toInt()}°",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.width(40.dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 快捷比例选择
        photoSize?.let { size ->
            Text(
                text = "目标尺寸: ${size.name} ${size.widthMm}×${size.heightMm}mm",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun FreeCropTab(
    rotation: Float,
    scale: Float,
    onRotationChange: (Float) -> Unit,
    onScaleChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 自由裁剪说明
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CropFree,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "自由裁剪",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "可自由调整裁剪区域和比例",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 缩放控制
        Text(
            text = "缩放",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Slider(
                value = scale,
                onValueChange = onScaleChange,
                valueRange = 0.5f..2.0f,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${scale.formatString(1)}x",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.width(50.dp),
                textAlign = TextAlign.End
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 旋转控制
        Text(
            text = "旋转",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Slider(
                value = rotation,
                onValueChange = onRotationChange,
                valueRange = -15f..15f,
                steps = 29,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${rotation.toInt()}°",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.width(50.dp),
                textAlign = TextAlign.End
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 重置按钮
        OutlinedButton(
            onClick = {
                onScaleChange(1f)
                onRotationChange(0f)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("重置所有调整")
        }
    }
}

@Composable
private fun CropRuleItem(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

/**
 * 裁剪结果数据类
 */
data class CropResult(
    val rotation: Float,      // 旋转角度
    val scale: Float,        // 缩放比例
    val offset: Offset,     // 偏移量
    val mode: CropMode,      // 裁剪模式
    val aspectRatio: Float   // 裁剪比例
)