import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import shared.navigation.App
import shared.navigation.AppState
import shared.navigation.rememberAppState
import shared.ui.theme.AppTheme

fun main() {
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "KMP Demo - Login",
            state = rememberWindowState(width = 400.dp, height = 700.dp),
            resizable = false
        ) {
            AppTheme {
                App(state = rememberAppState())
            }
        }
    }
}