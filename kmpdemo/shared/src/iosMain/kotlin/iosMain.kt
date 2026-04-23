package iosApp

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import shared.data.PhotoSizes
import shared.imageprocessing.ImagePickerHelper
import shared.navigation.App
import shared.navigation.rememberAppState
import shared.ui.theme.AppTheme
import platform.UIKit.UIViewController

private val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

fun MainViewController(): UIViewController {
    return ComposeUIViewController {
        AppTheme {
            val state = rememberAppState()

            // Set up image picker launcher
            val launchImagePicker: () -> Unit = {
                ImagePickerHelper.pickImage { imageData ->
                    mainScope.launch {
                        if (imageData != null) {
                            state.updateOriginalPhotoData(imageData)
                            val sizeToUse = state.pendingPhotoSize ?: PhotoSizes.allSizes.first()
                            state.onSizeSelected(sizeToUse)
                        }
                    }
                }
            }

            LaunchedEffect(Unit) {
                state.setupImagePickerLauncher(launchImagePicker)
            }

            App(state = state)
        }
    }
}