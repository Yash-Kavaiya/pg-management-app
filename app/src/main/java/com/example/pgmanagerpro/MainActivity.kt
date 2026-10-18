package com.example.pgmanagerpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pgmanagerpro.ui.MainLayout
import com.example.pgmanagerpro.ui.PGViewModel
import com.example.pgmanagerpro.ui.screens.AuthScreen
import com.example.pgmanagerpro.ui.theme.PGManagerProTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: PGViewModel = viewModel()
            val themeMode by viewModel.themeMode.collectAsState()
            val systemInDark = isSystemInDarkTheme()

            val isDark = when (themeMode) {
                "Dark" -> true
                "Light" -> false
                else -> systemInDark
            }

            PGManagerProTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var isAuthenticated by remember { mutableStateOf(true) }

                    if (isAuthenticated) {
                        MainLayout(
                            viewModel = viewModel,
                            onLogout = { isAuthenticated = false }
                        )
                    } else {
                        AuthScreen(
                            onLoginSuccess = { isAuthenticated = true }
                        )
                    }
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun AuthPreview() {
    PGManagerProTheme {
        AuthScreen(onLoginSuccess = {})
    }
}
