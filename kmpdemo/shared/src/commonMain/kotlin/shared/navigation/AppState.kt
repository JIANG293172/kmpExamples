package shared.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import shared.data.PhotoSize
import shared.data.PhotoSizes
import shared.ui.screens.LoginScreen
import shared.ui.screens.home.HomeScreen
import shared.ui.screens.home.SizeSelectScreen
import shared.ui.screens.home.PhotoEditorScreen
import shared.ui.screens.home.PhotoResultScreen
import shared.ui.screens.tools.ToolsScreen
import shared.ui.screens.explore.ExploreScreen
import shared.ui.screens.profile.ProfileScreen

/**
 * Navigation screens for the app
 */
enum class Screen {
    Login,
    Home,
    SizeSelect,
    PhotoEditor,
    PhotoResult,
    Tools,
    Explore,
    Profile
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

    var username by mutableStateOf("")
        private set

    var selectedPhotoSize by mutableStateOf<PhotoSize?>(null)
        private set

    var hasSelectedPhoto by mutableStateOf(false)
        private set

    var editedPhotoUri by mutableStateOf<String?>(null)
        private set

    // Bottom navigation index
    var bottomNavIndex by mutableStateOf(0)
        private set

    fun onLoginSuccess(user: String) {
        username = user
        currentScreen = Screen.Home
    }

    fun onLogout() {
        currentScreen = Screen.Login
        username = ""
        bottomNavIndex = 0
    }

    fun onNavigateToHome() {
        currentScreen = Screen.Home
        bottomNavIndex = 0
    }

    fun onNavigateToSizeSelect() {
        currentScreen = Screen.SizeSelect
    }

    fun onSizeSelected(size: PhotoSize) {
        selectedPhotoSize = size
        currentScreen = Screen.PhotoEditor
    }

    fun onPhotoSelected(uri: String) {
        hasSelectedPhoto = true
        editedPhotoUri = uri
    }

    fun onPhotoEdited(uri: String) {
        editedPhotoUri = uri
    }

    fun onNavigateToResult() {
        currentScreen = Screen.PhotoResult
    }

    fun onBackFromEditor() {
        if (selectedPhotoSize != null) {
            currentScreen = Screen.SizeSelect
        } else {
            currentScreen = Screen.Home
        }
    }

    fun onBackFromResult() {
        currentScreen = Screen.Home
        selectedPhotoSize = null
        hasSelectedPhoto = false
        editedPhotoUri = null
    }

    fun onBottomNavChange(index: Int) {
        bottomNavIndex = index
        currentScreen = when (index) {
            0 -> Screen.Home
            1 -> Screen.Tools
            2 -> Screen.Explore
            3 -> Screen.Profile
            else -> Screen.Home
        }
    }

    fun onNavigateToTools() {
        currentScreen = Screen.Tools
        bottomNavIndex = 1
    }

    fun onNavigateToExplore() {
        currentScreen = Screen.Explore
        bottomNavIndex = 2
    }

    fun onNavigateToProfile() {
        currentScreen = Screen.Profile
        bottomNavIndex = 3
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
                onLoginSuccess = { user ->
                    state.onLoginSuccess(user)
                }
            )
        }
        Screen.Home -> {
            HomeScreen(state = state)
        }
        Screen.SizeSelect -> {
            SizeSelectScreen(
                state = state,
                onBack = { state.onBackFromEditor() }
            )
        }
        Screen.PhotoEditor -> {
            PhotoEditorScreen(
                state = state,
                onBack = { state.onBackFromEditor() },
                onDone = { state.onNavigateToResult() }
            )
        }
        Screen.PhotoResult -> {
            PhotoResultScreen(
                state = state,
                onBack = { state.onBackFromResult() }
            )
        }
        Screen.Tools -> {
            ToolsScreen(state = state)
        }
        Screen.Explore -> {
            ExploreScreen(state = state)
        }
        Screen.Profile -> {
            ProfileScreen(
                username = state.username,
                onLogout = { state.onLogout() }
            )
        }
    }
}
