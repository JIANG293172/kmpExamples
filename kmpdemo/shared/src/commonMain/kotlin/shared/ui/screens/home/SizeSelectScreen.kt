package shared.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import shared.data.BackgroundColor
import shared.data.PhotoSize
import shared.data.PhotoSizes
import shared.data.SizeCategory
import shared.navigation.AppState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SizeSelectScreen(
    state: AppState,
    onBack: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf<SizeCategory?>(null) }
    var selectedBackground by remember { mutableStateOf(BackgroundColor.WHITE) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("选择证件照尺寸") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 尺寸分类
            item {
                Text(
                    text = "尺寸分类",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                CategoryChips(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it }
                )
            }

            // 尺寸列表
            val sizesToShow = if (selectedCategory == null) {
                PhotoSizes.allSizes
            } else {
                PhotoSizes.getByCategory(selectedCategory!!)
            }

            item {
                Text(
                    text = "可选尺寸 (${sizesToShow.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(sizesToShow.chunked(2)) { rowSizes ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowSizes.forEach { size ->
                        SizeCard(
                            size = size,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                state.onSizeSelected(size)
                            }
                        )
                    }
                    // 如果只有一项，补齐空白
                    if (rowSizes.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            // 背景颜色选择
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "背景颜色",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                BackgroundColorPicker(
                    selected = selectedBackground,
                    onColorSelected = { selectedBackground = it }
                )
            }

            // 照片选择按钮
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { /* 模拟选择照片 */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("相册选择")
                    }
                    Button(
                        onClick = { /* 模拟拍照 */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("拍照")
                    }
                }
            }

            // 拍摄示意说明
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "拍摄要求",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        RequirementItem("正脸清晰，表情自然", isGood = true)
                        RequirementItem("光线充足，避免阴影", isGood = true)
                        RequirementItem("着正装，避免白色服装", isGood = true)
                        RequirementItem("背景简洁，最好纯色背景", isGood = false)
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChips(
    selectedCategory: SizeCategory?,
    onCategorySelected: (SizeCategory?) -> Unit
) {
    val categories = listOf(
        null to "全部",
        SizeCategory.STANDARD to "标准",
        SizeCategory.VISA to "签证",
        SizeCategory.EXAM to "考试",
        SizeCategory.WORK to "工牌"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { (category, label) ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { Text(label) }
            )
        }
    }
}

@Composable
private fun SizeCard(
    size: PhotoSize,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = size.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = size.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${size.widthMm}×${size.heightMm}mm",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${size.widthPx}×${size.heightPx}px",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BackgroundColorPicker(
    selected: BackgroundColor,
    onColorSelected: (BackgroundColor) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BackgroundColor.entries.take(6).forEach { color ->
            ColorChip(
                color = color,
                isSelected = selected == color,
                onClick = { onColorSelected(color) }
            )
        }
    }
}

@Composable
private fun ColorChip(
    color: BackgroundColor,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(
                color = Color(color.colorValue),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .then(
                if (isSelected) {
                    Modifier.background(
                        color = Color.Black.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                Icons.Default.Check,
                contentDescription = "选中",
                tint = if (color == BackgroundColor.WHITE || color == BackgroundColor.GRAY)
                    Color.Black else Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun RequirementItem(
    text: String,
    isGood: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isGood) Icons.Default.CheckCircle else Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = if (isGood) Color(0xFF4CAF50) else Color(0xFFFF9800)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
