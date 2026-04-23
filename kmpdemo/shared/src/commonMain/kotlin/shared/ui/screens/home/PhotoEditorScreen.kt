package shared.ui.screens.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import shared.data.BackgroundColor
import shared.data.BackgroundType
import shared.data.ClothingTemplate
import shared.data.ClothingTemplates
import shared.data.ClothingType
import shared.imageprocessing.CropParams
import shared.imageprocessing.EnhanceParams
import shared.imageprocessing.ImageLoader
import shared.imageprocessing.ImageProcessor
import shared.imageprocessing.PhotoEditorViewModel
import shared.imageprocessing.createImageLoader
import shared.imageprocessing.rememberImageBitmap
import shared.ui.components.NativeImageDisplay
import shared.navigation.AppState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * 处理后照片的预览组件
 */
@Composable
private fun ProcessedPhotoPreview(
    imageData: ByteArray?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (imageData != null) {
            // 使用平台特定的原生图片显示组件
            NativeImageDisplay(
                imageData = imageData,
                modifier = Modifier.fillMaxSize(),
                onClick = onClick
            )
        } else {
            PlaceholderContent(onClick = onClick)
        }
    }
}

private fun Float.formatString(decimals: Int): String {
    val factor = 10.0.pow(decimals).toInt()
    val scaled = (this * factor).roundToInt()
    val whole = scaled / factor
    val frac = scaled % factor
    return "$whole.${frac.toString().padStart(decimals, '0')}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoEditorScreen(
    state: AppState,
    onBack: () -> Unit,
    onDone: () -> Unit,
    onCropClick: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedBackground by remember { mutableStateOf(BackgroundColor.BLUE) }
    var selectedBackgroundType by remember { mutableStateOf(BackgroundType.SOLID) }
    var selectedClothing by remember { mutableStateOf<ClothingTemplate?>(null) }
    var selectedClothingType by remember { mutableStateOf<ClothingType?>(null) }

    // 美化参数
    var brightness by remember { mutableFloatStateOf(0f) }
    var contrast by remember { mutableFloatStateOf(0f) }
    var sharpness by remember { mutableFloatStateOf(0f) }
    var denoise by remember { mutableFloatStateOf(0f) }
    var exposure by remember { mutableFloatStateOf(0f) }

    // 高级美颜参数（会员）
    var skinSmooth by remember { mutableFloatStateOf(0f) }
    var faceSlim by remember { mutableFloatStateOf(0f) }
    var eyeEnlarge by remember { mutableFloatStateOf(0f) }
    var eyeBrighten by remember { mutableFloatStateOf(0f) }

    // 处理相关
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // ViewModel for image processing
    val processingScope = remember { CoroutineScope(SupervisorJob() + Dispatchers.Main) }
    val processor = remember { ImageProcessor() }
    val imageLoader = remember { createImageLoader() }

    // Load image from URI when available
    LaunchedEffect(state.editedPhotoUri) {
        state.editedPhotoUri?.let { uri ->
            processingScope.launch {
                val imageData = imageLoader.loadFromUri(uri)
                if (imageData != null) {
                    state.updateOriginalPhotoData(imageData)
                }
            }
        }
    }

    // Initialize processor
    LaunchedEffect(Unit) {
        processor.initialize()
    }

    // 清理
    DisposableEffect(Unit) {
        onDispose {
            processor.release()
        }
    }

    // Process photo with selected background
    fun processPhoto() {
        val originalData = state.originalPhotoData ?: return
        isProcessing = true
        errorMessage = null

        processingScope.launch {
            val result = processor.process证件照(originalData, selectedBackground)
            isProcessing = false
            if (result.success && result.data != null) {
                state.updateProcessedPhotoData(result.data)
            } else {
                errorMessage = result.errorMessage ?: "处理失败"
            }
        }
    }

    // Apply enhancement
    fun applyEnhancement() {
        val imageData = state.processedPhotoData ?: state.originalPhotoData ?: return
        isProcessing = true
        errorMessage = null

        processingScope.launch {
            val params = EnhanceParams(
                brightness = brightness,
                contrast = contrast,
                sharpness = sharpness,
                denoise = denoise,
                exposure = exposure
            )
            val result = processor.enhance(imageData, params)
            isProcessing = false
            if (result.success && result.data != null) {
                state.updateProcessedPhotoData(result.data)
            } else {
                errorMessage = result.errorMessage ?: "美化处理失败"
            }
        }
    }

    // Complete processing
    fun processComplete() {
        val originalData = state.originalPhotoData ?: return
        isProcessing = true
        errorMessage = null

        processingScope.launch {
            val cropParams = state.cropResult?.let {
                CropParams(
                    x = 0, y = 0,
                    width = 0, height = 0,
                    rotation = it.rotation,
                    scale = it.scale
                )
            }
            val enhanceParams = EnhanceParams(
                brightness = brightness,
                contrast = contrast,
                sharpness = sharpness,
                denoise = denoise,
                exposure = exposure
            )
            val result = processor.processComplete(
                originalData,
                selectedBackground,
                cropParams,
                enhanceParams
            )
            isProcessing = false
            if (result.success && result.data != null) {
                state.updateProcessedPhotoData(result.data)
                onDone()
            } else {
                errorMessage = result.errorMessage ?: "处理失败"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("编辑证件照") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 处理中的指示器
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    IconButton(onClick = onCropClick) {
                        Icon(Icons.Default.Crop, contentDescription = "裁剪")
                    }
                    TextButton(
                        onClick = { processComplete() },
                        enabled = !isProcessing && state.originalPhotoData != null
                    ) {
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
            // 照片预览区域
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                PhotoPreviewWithProcessing(
                    backgroundColor = selectedBackground,
                    backgroundType = selectedBackgroundType,
                    hasPhoto = state.originalPhotoData != null,
                    processedImageData = state.processedPhotoData,
                    isProcessing = isProcessing,
                    onPickImage = { state.triggerImagePicker() }
                )
            }

            // 错误提示
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { errorMessage = null }) {
                            Icon(Icons.Default.Close, "关闭")
                        }
                    }
                }
            }

            // 编辑工具区域
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Tab选择器
                    TabRow(
                        selectedTabIndex = selectedTab
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("背景") },
                            icon = { Icon(Icons.Default.FormatColorFill, null) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("服装") },
                            icon = { Icon(Icons.Default.Checkroom, null) }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text("美化") },
                            icon = { Icon(Icons.Default.Tune, null) }
                        )
                    }

                    // Tab内容
                    when (selectedTab) {
                        0 -> BackgroundTab(
                            selected = selectedBackground,
                            selectedType = selectedBackgroundType,
                            onSelect = {
                                selectedBackground = it
                                if (state.originalPhotoData != null) {
                                    processPhoto()
                                }
                            },
                            onTypeSelect = { selectedBackgroundType = it },
                            enabled = state.originalPhotoData != null && !isProcessing
                        )
                        1 -> ClothingTab(
                            selected = selectedClothing,
                            selectedType = selectedClothingType,
                            onSelect = { selectedClothing = it },
                            onTypeSelect = { selectedClothingType = it }
                        )
                        2 -> EnhanceTab(
                            brightness = brightness,
                            contrast = contrast,
                            sharpness = sharpness,
                            denoise = denoise,
                            exposure = exposure,
                            skinSmooth = skinSmooth,
                            faceSlim = faceSlim,
                            eyeEnlarge = eyeEnlarge,
                            eyeBrighten = eyeBrighten,
                            onBrightnessChange = { brightness = it },
                            onContrastChange = { contrast = it },
                            onSharpnessChange = { sharpness = it },
                            onDenoiseChange = { denoise = it },
                            onExposureChange = { exposure = it },
                            onSkinSmoothChange = { skinSmooth = it },
                            onFaceSlimChange = { faceSlim = it },
                            onEyeEnlargeChange = { eyeEnlarge = it },
                            onEyeBrightenChange = { eyeBrighten = it },
                            onAutoEnhance = {
                                brightness = 0.1f
                                contrast = 0.1f
                                sharpness = 0.2f
                                exposure = 0.05f
                            },
                            onApply = { applyEnhancement() },
                            enabled = state.originalPhotoData != null && !isProcessing
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoPreviewWithProcessing(
    backgroundColor: BackgroundColor,
    backgroundType: BackgroundType,
    hasPhoto: Boolean,
    processedImageData: ByteArray?,
    isProcessing: Boolean,
    onPickImage: () -> Unit
) {
    val backgroundModifier = when (backgroundType) {
        BackgroundType.SOLID -> Modifier.background(Color(backgroundColor.colorValue))
        BackgroundType.GRADIENT -> {
            val colors = backgroundColor.gradientColors ?: listOf(backgroundColor.colorValue, backgroundColor.colorValue)
            Modifier.background(
                Brush.linearGradient(
                    colors = colors.map { Color(it) }
                )
            )
        }
        BackgroundType.TRANSPARENT -> Modifier.background(
            checkerboardPattern()
        )
    }

    Box(
        modifier = Modifier
            .size(280.dp)
            .clip(RoundedCornerShape(16.dp))
            .then(backgroundModifier),
        contentAlignment = Alignment.Center
    ) {
        when {
            isProcessing -> {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
            processedImageData != null -> {
                // 显示处理后的图片
                ProcessedPhotoPreview(
                    imageData = processedImageData,
                    modifier = Modifier.fillMaxSize(),
                    onClick = onPickImage
                )
            }
            hasPhoto -> {
                // 照片加载中或正在处理
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
            else -> {
                PlaceholderContent(onClick = onPickImage)
            }
        }
    }
}

@Composable
private fun PlaceholderContent(onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.AddAPhoto,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "请选择或拍摄照片",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Blue
        )
    }
}

@Composable
private fun checkerboardPattern(): Brush {
    return Brush.linearGradient(
        colors = listOf(
            Color.LightGray,
            Color.White,
            Color.LightGray
        )
    )
}

@Composable
private fun BackgroundTab(
    selected: BackgroundColor,
    selectedType: BackgroundType,
    onSelect: (BackgroundColor) -> Unit,
    onTypeSelect: (BackgroundType) -> Unit,
    enabled: Boolean = true
) {
    var showCustomBackgroundDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 背景类型选择
        Text(
            text = "背景类型",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BackgroundTypeChip(
                label = "纯色",
                icon = Icons.Default.Square,
                isSelected = selectedType == BackgroundType.SOLID,
                onClick = { onTypeSelect(BackgroundType.SOLID) },
                modifier = Modifier.weight(1f),
                enabled = enabled
            )
            BackgroundTypeChip(
                label = "渐变",
                icon = Icons.Default.Gradient,
                isSelected = selectedType == BackgroundType.GRADIENT,
                onClick = { onTypeSelect(BackgroundType.GRADIENT) },
                modifier = Modifier.weight(1f),
                enabled = enabled
            )
            BackgroundTypeChip(
                label = "透明",
                icon = Icons.Default.Layers,
                isSelected = selectedType == BackgroundType.TRANSPARENT,
                onClick = { onTypeSelect(BackgroundType.TRANSPARENT) },
                modifier = Modifier.weight(1f),
                enabled = enabled
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 颜色选择
        Text(
            text = if (selectedType == BackgroundType.GRADIENT) "渐变颜色" else "背景颜色",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        val colorsToShow = when (selectedType) {
            BackgroundType.SOLID -> BackgroundColor.getSolidColors()
            BackgroundType.GRADIENT -> BackgroundColor.getGradientColors()
            BackgroundType.TRANSPARENT -> listOf(BackgroundColor.TRANSPARENT)
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(colorsToShow) { color ->
                BackgroundColorItem(
                    color = color,
                    backgroundType = selectedType,
                    isSelected = selected == color,
                    onClick = { if (enabled) onSelect(color) }
                )
            }

            // 自定义颜色按钮
            item {
                CustomBackgroundItem(
                    onClick = { showCustomBackgroundDialog = true },
                    enabled = enabled
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // AI抠图说明
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
                        text = "AI智能抠图",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "发丝级抠图，边缘自然过渡",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // 适用场景说明
        if (selected != BackgroundColor.TRANSPARENT) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "适用场景: ${getApplicableScene(selected)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    // 自定义颜色对话框
    if (showCustomBackgroundDialog) {
        AlertDialog(
            onDismissRequest = { showCustomBackgroundDialog = false },
            title = { Text("自定义背景") },
            text = {
                Column {
                    Text("可选择纯色或从相册导入图片作为背景")
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { /* TODO: 选择纯色 */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Square, null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("纯色")
                        }
                        OutlinedButton(
                            onClick = { /* TODO: 从相册选择 */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Image, null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("图片")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCustomBackgroundDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun BackgroundTypeChip(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        },
        modifier = modifier,
        enabled = enabled
    )
}

@Composable
private fun BackgroundColorItem(
    color: BackgroundColor,
    backgroundType: BackgroundType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .then(
                    if (backgroundType == BackgroundType.GRADIENT && color.gradientColors != null) {
                        Modifier.background(
                            Brush.linearGradient(color.gradientColors.map { Color(it) })
                        )
                    } else if (backgroundType == BackgroundType.TRANSPARENT) {
                        Modifier.background(
                            Brush.linearGradient(
                                colors = listOf(Color.LightGray, Color.White)
                            )
                        )
                    } else {
                        Modifier.background(Color(color.colorValue))
                    }
                )
                .then(
                    if (isSelected) {
                        Modifier.border(
                            width = 3.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(12.dp)
                        )
                    } else Modifier
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "选中",
                    tint = if (color == BackgroundColor.WHITE || color == BackgroundColor.LIGHT_GRAY)
                        Color.Black else Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else if (backgroundType == BackgroundType.TRANSPARENT) {
                Icon(
                    Icons.Default.Layers,
                    contentDescription = "透明",
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = color.displayName,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun CustomBackgroundItem(
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable(enabled = enabled, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "自定义",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "自定义",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

private fun getApplicableScene(color: BackgroundColor): String {
    return when (color) {
        BackgroundColor.WHITE -> "身份证、护照、简历"
        BackgroundColor.RED -> "党员证、学生证"
        BackgroundColor.BLUE -> "工作证、毕业证"
        BackgroundColor.LIGHT_BLUE -> "医保、签证"
        BackgroundColor.LIGHT_GRAY -> "公务员、考试"
        BackgroundColor.DARK_RED -> "港澳通行证"
        BackgroundColor.DARK_BLUE -> "护照、签证"
        BackgroundColor.PALE_BLUE -> "签证、护照"
        else -> "通用"
    }
}

@Composable
private fun ClothingTab(
    selected: ClothingTemplate?,
    selectedType: ClothingType?,
    onSelect: (ClothingTemplate?) -> Unit,
    onTypeSelect: (ClothingType?) -> Unit
) {
    var showPremiumDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 服装类型选择
        Text(
            text = "服装类型",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedType == null,
                onClick = { onTypeSelect(null) },
                label = { Text("全部") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = selectedType == ClothingType.MEN,
                onClick = { onTypeSelect(ClothingType.MEN) },
                label = { Text("男装") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = selectedType == ClothingType.WOMEN,
                onClick = { onTypeSelect(ClothingType.WOMEN) },
                label = { Text("女装") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = selectedType == ClothingType.STUDENT,
                onClick = { onTypeSelect(ClothingType.STUDENT) },
                label = { Text("学生装") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 服装列表
        val templates = if (selectedType == null) {
            ClothingTemplates.allTemplates
        } else {
            ClothingTemplates.getByType(selectedType)
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(templates) { template ->
                ClothingItem(
                    template = template,
                    isSelected = selected == template,
                    onClick = {
                        if (!template.isFree) {
                            showPremiumDialog = true
                        } else {
                            onSelect(if (selected == template) null else template)
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 当前选择
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (selected != null) "已选择: ${selected.name}" else "请选择服装",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        if (selected != null) {
                            Text(
                                text = selected.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (selected != null) {
                        IconButton(onClick = { onSelect(null) }) {
                            Icon(Icons.Default.Close, "清除")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 服装替换说明
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
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "自动贴合肩部，边缘自然融合",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }

    // 会员提示对话框
    if (showPremiumDialog) {
        AlertDialog(
            onDismissRequest = { showPremiumDialog = false },
            icon = { Icon(Icons.Default.WorkspacePremium, null) },
            title = { Text("会员专享") },
            text = {
                Column {
                    Text("该服装模板为会员专享内容")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "开通会员可解锁全部服装模板",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showPremiumDialog = false }) {
                    Text("立即开通")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPremiumDialog = false }) {
                    Text("稍后")
                }
            }
        )
    }
}

@Composable
private fun ClothingItem(
    template: ClothingTemplate,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .then(
                    if (isSelected) {
                        Modifier.border(
                            width = 3.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(12.dp)
                        )
                    } else Modifier
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Checkroom,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                if (!template.isFree) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "付费",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = template.name,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        if (!template.isFree) {
            Text(
                text = "VIP",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
private fun EnhanceTab(
    brightness: Float,
    contrast: Float,
    sharpness: Float,
    denoise: Float,
    exposure: Float,
    skinSmooth: Float,
    faceSlim: Float,
    eyeEnlarge: Float,
    eyeBrighten: Float,
    onBrightnessChange: (Float) -> Unit,
    onContrastChange: (Float) -> Unit,
    onSharpnessChange: (Float) -> Unit,
    onDenoiseChange: (Float) -> Unit,
    onExposureChange: (Float) -> Unit,
    onSkinSmoothChange: (Float) -> Unit,
    onFaceSlimChange: (Float) -> Unit,
    onEyeEnlargeChange: (Float) -> Unit,
    onEyeBrightenChange: (Float) -> Unit,
    onAutoEnhance: () -> Unit,
    onApply: () -> Unit,
    enabled: Boolean = true
) {
    var showAdvanced by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 基础美化
        Text(
            text = "画质增强",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        // 智能曝光
        EnhanceSlider(
            icon = Icons.Default.WbSunny,
            label = "智能曝光",
            value = exposure,
            onValueChange = onExposureChange,
            valueRange = -0.2f..0.2f,
            infoText = "自动校正亮度 ±20%",
            enabled = enabled
        )

        // 亮度
        EnhanceSlider(
            icon = Icons.Default.Brightness6,
            label = "亮度",
            value = brightness,
            onValueChange = onBrightnessChange,
            valueRange = -1f..1f,
            enabled = enabled
        )

        // 对比度
        EnhanceSlider(
            icon = Icons.Default.Contrast,
            label = "对比度",
            value = contrast,
            onValueChange = onContrastChange,
            valueRange = -1f..1f,
            infoText = "自动优化 ±15%",
            enabled = enabled
        )

        // 锐化
        EnhanceSlider(
            icon = Icons.Default.ShutterSpeed,
            label = "锐化",
            value = sharpness,
            onValueChange = onSharpnessChange,
            valueRange = 0f..1f,
            infoText = "0.1~0.3 轻度增强五官",
            enabled = enabled
        )

        // 降噪
        EnhanceSlider(
            icon = Icons.Default.NoiseAware,
            label = "降噪",
            value = denoise,
            onValueChange = onDenoiseChange,
            valueRange = 0f..1f,
            infoText = "去除夜间噪点",
            enabled = enabled
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 高级美颜切换
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "高级美颜",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            FilterChip(
                selected = showAdvanced,
                onClick = { showAdvanced = !showAdvanced },
                label = { Text(if (showAdvanced) "收起" else "展开") },
                leadingIcon = {
                    Icon(
                        if (showAdvanced) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }

        // 高级美颜选项（会员）
        if (showAdvanced) {
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "会员专享功能",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 磨皮
            EnhanceSlider(
                icon = Icons.Default.Face,
                label = "磨皮",
                value = skinSmooth,
                onValueChange = onSkinSmoothChange,
                valueRange = 0f..0.3f,
                infoText = "保留毛孔，自然美肤",
                isPremium = true
            )

            // 瘦脸
            EnhanceSlider(
                icon = Icons.Default.Face,
                label = "瘦脸",
                value = faceSlim,
                onValueChange = onFaceSlimChange,
                valueRange = 0f..0.2f,
                infoText = "禁止过度变形",
                isPremium = true
            )

            // 大眼
            EnhanceSlider(
                icon = Icons.Default.Visibility,
                label = "大眼",
                value = eyeEnlarge,
                onValueChange = onEyeEnlargeChange,
                valueRange = 0f..0.15f,
                infoText = "0~15% 自然放大",
                isPremium = true
            )

            // 亮眼
            EnhanceSlider(
                icon = Icons.Default.AutoAwesome,
                label = "亮眼",
                value = eyeBrighten,
                onValueChange = onEyeBrightenChange,
                valueRange = 0f..0.25f,
                isPremium = true
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 快捷按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    onBrightnessChange(0f)
                    onContrastChange(0f)
                    onSharpnessChange(0f)
                    onDenoiseChange(0f)
                    onExposureChange(0f)
                },
                modifier = Modifier.weight(1f),
                enabled = enabled
            ) {
                Text("重置")
            }
            Button(
                onClick = onAutoEnhance,
                modifier = Modifier.weight(1f),
                enabled = enabled
            ) {
                Icon(Icons.Default.AutoFixHigh, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("自动增强")
            }
            Button(
                onClick = onApply,
                modifier = Modifier.weight(1f),
                enabled = enabled
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("应用")
            }
        }
    }
}

@Composable
private fun EnhanceSlider(
    icon: ImageVector,
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    infoText: String? = null,
    isPremium: Boolean = false,
    enabled: Boolean = true
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (isPremium) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.width(60.dp)
            )
            if (isPremium) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "付费",
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                modifier = Modifier.weight(1f),
                enabled = enabled
            )
            Text(
                text = "${(value * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.width(40.dp),
                textAlign = TextAlign.End
            )
        }
        if (infoText != null) {
            Text(
                text = infoText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 36.dp)
            )
        }
    }
}