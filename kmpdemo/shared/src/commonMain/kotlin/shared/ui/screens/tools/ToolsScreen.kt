package shared.ui.screens.tools

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import shared.navigation.AppState
import shared.ui.components.BottomNavBar

data class ToolItem(
    val title: String,
    val description: String,
    val icon: ImageVector
)

val tools = listOf(
    ToolItem("照片压缩", "减小照片体积", Icons.Default.Compress),
    ToolItem("格式转换", "JPG/PNG互转", Icons.Default.SwapHoriz),
    ToolItem("老照片修复", "修复模糊老照片", Icons.Default.AutoFixHigh),
    ToolItem("证件照美化", "自然美颜优化", Icons.Default.Face),
    ToolItem("去水印", "去除照片水印", Icons.Default.WaterDrop),
    ToolItem("添加水印", "保护照片版权", Icons.Default.BrandingWatermark),
    ToolItem("照片拼图", "多张照片拼接", Icons.Default.PhotoLibrary),
    ToolItem("名片制作", "制作电子名片", Icons.Default.ContactPage),
    ToolItem("简历制作", "制作精美简历", Icons.Default.Description),
    ToolItem("更多工具", "更多实用功能", Icons.Default.MoreHoriz)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 标题
            Text(
                text = "工具箱",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            // 工具网格
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(tools) { tool ->
                    ToolCard(tool = tool)
                }
            }
        }
    }
}

@Composable
private fun ToolCard(
    tool: ToolItem
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* 打开工具 */ },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tool.icon,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = tool.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Text(
                text = tool.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
