package iosApp

import androidx.compose.ui.window.ComposeUIViewController
import shared.navigation.App
import shared.navigation.rememberAppState
import shared.ui.theme.AppTheme
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    return ComposeUIViewController {
        AppTheme {
            App(state = rememberAppState())
        }
    }
}
