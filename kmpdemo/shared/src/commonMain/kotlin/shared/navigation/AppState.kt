package shared.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import shared.ui.screens.LoginScreen
import shared.ui.screens.LoginSuccessScreen

/**
 * Navigation screens for the app
 */
enum class Screen {
    Login,
    Success
}

/**
 * Shared navigation state holder.
 * This is placed in commonMain so Android, iOS, and Desktop can all use it.
 */
@Composable
fun rememberAppState(): AppState {
    return remember { AppState() }
}

class AppState {
    var currentScreen by mutableStateOf(Screen.Login)
        private set

    var userEmail by mutableStateOf("")
        private set

    fun onLoginSuccess(email: String) {
        userEmail = email
        currentScreen = Screen.Success
    }

    fun onLogout() {
        currentScreen = Screen.Login
        userEmail = ""
    }
}

/**
 * Shared App composable that handles navigation.
 * This is the main entry point for the shared UI.
 */
@Composable
fun App(
    state: AppState = rememberAppState()
) {
    when (state.currentScreen) {
        Screen.Login -> {
            LoginScreen(
                onLoginSuccess = { email ->
                    state.onLoginSuccess(email)
                }
            )
        }
        Screen.Success -> {
            LoginSuccessScreen(
                userEmail = state.userEmail,
                onLogout = {
                    state.onLogout()
                }
            )
        }
    }
}