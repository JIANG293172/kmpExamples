package shared.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import shared.data.PhotoSizes
import shared.navigation.AppState
import shared.ui.components.BottomNavBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: AppState
) {
    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentIndex = state.bottomNavIndex,
                onItemSelected = { state.onBottomNavChange(it) }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // 搜索栏
            item {
                SearchBar()
            }

            // 快捷尺寸入口
            item {
                QuickSizeSection(
                    onSizeClick = { size ->
                        state.onSizeSelected(size)
                    }
                )
            }

            // 编辑工具
            item {
                EditToolsSection(
                    onBackgroundClick = { state.onNavigateToSizeSelect() },
                    onClothingClick = { state.onNavigateToSizeSelect() },
                    onCropClick = { state.onNavigateToSizeSelect() },
                    onEnhanceClick = { state.onNavigateToSizeSelect() }
                )
            }

            // 活动Banner
            item {
                BannerSection()
            }

            // 热门模板
            item {
                HotTemplatesSection(
                    onTemplateClick = { state.onNavigateToSizeSelect() }
                )
            }
        }
    }
}

@Composable
private fun SearchBar() {
    var searchText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("搜索证件照尺寸，如：一寸照、签证照") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "搜索")
            },
            trailingIcon = {
                if (searchText.isNotEmpty()) {
                    IconButton(onClick = { searchText = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "清除")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
private fun QuickSizeSection(
    onSizeClick: (shared.data.PhotoSize) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "常用尺寸",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = { /* 查看更多 */ }) {
                Text("更多尺寸")
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickSizeCard(
                title = "一寸照",
                subtitle = "25×35mm",
                icon = Icons.Default.Photo,
                modifier = Modifier.weight(1f),
                onClick = { onSizeClick(PhotoSizes.oneInch) }
            )
            QuickSizeCard(
                title = "二寸照",
                subtitle = "35×49mm",
                icon = Icons.Default.Photo,
                modifier = Modifier.weight(1f),
                onClick = { onSizeClick(PhotoSizes.twoInch) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickSizeCard(
                title = "签证照",
                subtitle = "多国签证",
                icon = Icons.Default.Flight,
                modifier = Modifier.weight(1f),
                onClick = { onSizeClick(PhotoSizes.visaUS) }
            )
            QuickSizeCard(
                title = "考试照",
                subtitle = "报名专用",
                icon = Icons.Default.School,
                modifier = Modifier.weight(1f),
                onClick = { onSizeClick(PhotoSizes.examGaokao) }
            )
        }
    }
}

@Composable
private fun QuickSizeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EditToolsSection(
    onBackgroundClick: () -> Unit,
    onClothingClick: () -> Unit,
    onCropClick: () -> Unit,
    onEnhanceClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "基础编辑工具",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EditToolCard(
                title = "换背景",
                icon = Icons.Default.FormatColorFill,
                modifier = Modifier.weight(1f),
                onClick = onBackgroundClick
            )
            EditToolCard(
                title = "换服装",
                icon = Icons.Default.Checkroom,
                modifier = Modifier.weight(1f),
                onClick = onClothingClick
            )
            EditToolCard(
                title = "裁剪",
                icon = Icons.Default.Crop,
                modifier = Modifier.weight(1f),
                onClick = onCropClick
            )
            EditToolCard(
                title = "画质修复",
                icon = Icons.Default.AutoFixHigh,
                modifier = Modifier.weight(1f),
                onClick = onEnhanceClick
            )
        }
    }
}

@Composable
private fun EditToolCard(
    title: String,
    icon: ImageVector,
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
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BannerSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "无损放大",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "细节不模糊",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("立即体验")
                    }
                }
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = Color.White.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
private fun HotTemplatesSection(
    onTemplateClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "热门模板推荐",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = { }) {
                Text("查看全部")
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(listOf(
                "职业证件照" to Icons.Default.Work,
                "简历形象照" to Icons.Default.Description,
                "社交头像照" to Icons.Default.Face,
                "商务正装照" to Icons.Default.BusinessCenter
            )) { (title, icon) ->
                HotTemplateCard(
                    title = title,
                    icon = icon,
                    onClick = onTemplateClick
                )
            }
        }
    }
}

@Composable
private fun HotTemplateCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "点击制作",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
