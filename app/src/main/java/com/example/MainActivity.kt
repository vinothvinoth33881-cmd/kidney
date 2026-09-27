package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.MedicalBottomNav
import com.example.ui.components.MedicalTopBar
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DiagnosisScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ModelInfoScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ResultScreen
import com.example.ui.theme.KidneyAITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = viewModel()
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()
            val isLoggedIn by viewModel.isLoggedIn.collectAsState()

            KidneyAITheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (!isLoggedIn || currentScreen == AppScreen.AUTH) {
                        AuthScreen(
                            viewModel = viewModel,
                            onNavigate = { viewModel.navigateTo(it) }
                        )
                    } else {
                        // Handle back button behavior
                        BackHandler(enabled = currentScreen != AppScreen.HOME) {
                            if (currentScreen == AppScreen.RESULT) {
                                viewModel.navigateTo(AppScreen.DIAGNOSIS)
                            } else {
                                viewModel.navigateTo(AppScreen.HOME)
                            }
                        }

                        Scaffold(
                            topBar = {
                                MedicalTopBar(
                                    currentScreen = currentScreen,
                                    isDarkTheme = isDarkTheme,
                                    onToggleTheme = { viewModel.toggleTheme() },
                                    onProfileClick = { viewModel.navigateTo(AppScreen.PROFILE) }
                                )
                            },
                            bottomBar = {
                                MedicalBottomNav(
                                    currentScreen = currentScreen,
                                    onNavigate = { viewModel.navigateTo(it) }
                                )
                            }
                        ) { innerPadding ->
                            androidx.compose.foundation.layout.Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                when (currentScreen) {
                                    AppScreen.HOME -> HomeScreen(
                                        viewModel = viewModel,
                                        onNavigate = { viewModel.navigateTo(it) }
                                    )
                                    AppScreen.DIAGNOSIS -> DiagnosisScreen(
                                        viewModel = viewModel
                                    )
                                    AppScreen.RESULT -> ResultScreen(
                                        viewModel = viewModel,
                                        onNavigate = { viewModel.navigateTo(it) }
                                    )
                                    AppScreen.HISTORY -> HistoryScreen(
                                        viewModel = viewModel,
                                        onNavigate = { viewModel.navigateTo(it) }
                                    )
                                    AppScreen.MODEL_INFO -> ModelInfoScreen()
                                    AppScreen.ABOUT -> AboutScreen()
                                    AppScreen.PROFILE -> ProfileScreen(
                                        viewModel = viewModel,
                                        onNavigate = { viewModel.navigateTo(it) }
                                    )
                                    AppScreen.AUTH -> AuthScreen(
                                        viewModel = viewModel,
                                        onNavigate = { viewModel.navigateTo(it) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
