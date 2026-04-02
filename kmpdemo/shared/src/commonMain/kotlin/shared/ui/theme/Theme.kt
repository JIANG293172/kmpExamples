package shared.ui.theme

import androidx.compose.runtime.Composable

/**
 * Expected theme composable for the app.
 * Each platform provides its own implementation with platform-specific theming.
 */
@Composable
expect fun AppTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
)