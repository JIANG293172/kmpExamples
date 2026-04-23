package com.example.kmpdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import shared.navigation.App
import shared.navigation.rememberAppState
import shared.ui.theme.AppTheme
import shared.imageprocessing.rememberImagePickerLauncher
import shared.data.PhotoSizes

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                val state = rememberAppState()

                // 设置图片选择器
                val launchImagePicker = rememberImagePickerLauncher(
                    onImageSelected = { imageData ->
                        if (imageData != null) {
                            state.updateOriginalPhotoData(imageData)
                            // 使用待处理的尺寸导航到编辑器
                            val sizeToUse = state.pendingPhotoSize ?: PhotoSizes.allSizes.first()
                            state.onSizeSelected(sizeToUse)
                        }
                    }
                )

                // 将图片选择器启动器设置到 AppState（只在首次组合时执行）
                LaunchedEffect(Unit) {
                    state.setupImagePickerLauncher(launchImagePicker)
                }

                App(state = state)
            }
        }
    }
}