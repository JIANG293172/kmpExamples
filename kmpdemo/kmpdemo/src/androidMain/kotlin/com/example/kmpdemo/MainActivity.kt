package com.example.kmpdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kmpdemo.ui.theme.KmpdemoTheme
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KmpdemoTheme {
                App()
            }
        }
    }
}
