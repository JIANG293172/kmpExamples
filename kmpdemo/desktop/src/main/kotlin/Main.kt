import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kmpdemo.LoginScreen
import kmpdemo.LoginSuccessScreen

enum class Screen {
    Login,
    Success
}

@Composable
fun App(modifier: Modifier = Modifier) {
    var currentScreen by remember { mutableStateOf(Screen.Login) }
    var userEmail by remember { mutableStateOf("") }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (currentScreen) {
            Screen.Login -> {
                LoginScreen(
                    onLoginSuccess = { email ->
                        userEmail = email
                        currentScreen = Screen.Success
                    }
                )
            }
            Screen.Success -> {
                LoginSuccessScreen(
                    userEmail = userEmail,
                    onLogout = {
                        currentScreen = Screen.Login
                        userEmail = ""
                    }
                )
            }
        }
    }
}

fun main() {
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "KMP Demo - Login",
            state = rememberWindowState(width = 400.dp, height = 700.dp),
            resizable = false
        ) {
            App()
        }
    }
}
