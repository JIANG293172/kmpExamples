package shared.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import shared.data.BackgroundColor
import shared.data.ClothingTemplate
import shared.data.ClothingTemplates
import shared.data.PhotoSize
import shared.navigation.AppState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoEditorScreen(
    state: AppState,
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var selectedBackground by remember { mutableStateOf(BackgroundColor.BLUE) }
    var selectedClothing by remember { mutableStateOf<ClothingTemplate?>(null) }
    var brightness by remember { mutableFloatStateOf(0f) }
    var contrast by remember { mutableFloatStateOf(0f) }

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
                    TextButton(onClick = onDone) {
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
                PhotoPreview(
                    backgroundColor = selectedBackground,
                    hasPhoto = state.hasSelectedPhoto
                )
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
                            onSelect = { selectedBackground = it }
                        )
                        1 -> ClothingTab(
                            selected = selectedClothing,
                            onSelect = { selectedClothing = it }
                        )
                        2 -> EnhanceTab(
                            brightness = brightness,
                            contrast = contrast,
                            onBrightnessChange = { brightness = it },
                            onContrastChange = { contrast = it }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoPreview(
    backgroundColor: BackgroundColor,
    hasPhoto: Boolean
) {
    Box(
        modifier = Modifier
            .size(280.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(backgroundColor.colorValue)),
        contentAlignment = Alignment.Center
    ) {
        if (hasPhoto) {
            // 模拟照片内容
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Face,
                        contentDescription = null,
                        modifier = Modifier.size(120.dp),
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "证件照预览",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.AddAPhoto,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = if (backgroundColor == BackgroundColor.WHITE || backgroundColor == BackgroundColor.GRAY)
                        Color.Gray else Color.White.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "请选择或拍摄照片",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (backgroundColor == BackgroundColor.WHITE || backgroundColor == BackgroundColor.GRAY)
                        Color.Gray else Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun BackgroundTab(
    selected: BackgroundColor,
    onSelect: (BackgroundColor) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "选择背景颜色",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(BackgroundColor.entries.toList()) { color ->
                BackgroundColorItem(
                    color = color,
                    isSelected = selected == color,
                    onClick = { onSelect(color) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AI智能抠图，自动分离人物与背景",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun BackgroundColorItem(
    color: BackgroundColor,
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
                .background(Color(color.colorValue))
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
                    tint = if (color == BackgroundColor.WHITE || color == BackgroundColor.GRAY)
                        Color.Black else Color.White,
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
private fun ClothingTab(
    selected: ClothingTemplate?,
    onSelect: (ClothingTemplate?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "选择服装模板",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(ClothingTemplates.templates) { template ->
                ClothingItem(
                    template = template,
                    isSelected = selected == template,
                    onClick = { onSelect(if (selected == template) null else template) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

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
        }
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
            Icon(
                Icons.Default.Checkroom,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = template.name,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EnhanceTab(
    brightness: Float,
    contrast: Float,
    onBrightnessChange: (Float) -> Unit,
    onContrastChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "画质增强",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        // 亮度
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.WbSunny,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "亮度",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.width(50.dp)
            )
            Slider(
                value = brightness,
                onValueChange = onBrightnessChange,
                valueRange = -1f..1f,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${(brightness * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.width(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 对比度
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Contrast,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "对比度",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.width(50.dp)
            )
            Slider(
                value = contrast,
                onValueChange = onContrastChange,
                valueRange = -1f..1f,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${(contrast * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.width(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 快捷按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    onBrightnessChange(0f)
                    onContrastChange(0f)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("重置")
            }
            Button(
                onClick = { /* 自动增强 */ },
                modifier = Modifier.weight(1f)
            ) {
                Text("自动增强")
            }
        }
    }
}
