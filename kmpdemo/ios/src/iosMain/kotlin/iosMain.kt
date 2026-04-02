package iosApp

import androidx.compose.ui.window.ComposeUIViewController
import shared.navigation.AppState
import shared.navigation.rememberAppState
import shared.ui.theme.AppTheme

fun MainViewController(): ComposeUIViewController {
    return ComposeUIViewController {
        AppTheme {
            App(state = rememberAppState())
        }
    }
}