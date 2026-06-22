package com.sahilmaske.peerlearn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sahilmaske.peerlearn.ui.Profile.ProfileSetupScreen
import com.sahilmaske.peerlearn.ui.auth.LoginScreen
import com.sahilmaske.peerlearn.ui.auth.RegisterScreen
import com.sahilmaske.peerlearn.ui.home.HomeScreen
import com.sahilmaske.peerlearn.ui.theme.PeerLearnTheme
import com.sahilmaske.peerlearn.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PeerLearnTheme {
                val navController = rememberNavController()
                val authViewModel = AuthViewModel()

                NavHost(
                    navController = navController,
                    startDestination = if (authViewModel.isUserLoggedIn) "home" else "register",
                    enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                    exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
                    popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
                    popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
                ) {
                    composable("login") {
                        LoginScreen(
                            viewModel = authViewModel,
                            onLoginSuccess = {
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onNavigateToRegister = {
                                navController.navigate("register")
                            }
                        )
                    }
                    composable("home") {
                        HomeScreen()
                    }
                    composable("register") {
                        RegisterScreen(
                            viewModel = authViewModel,
                            onRegisterSuccess = {
                                navController.navigate("profile") {
                                    popUpTo("register") { inclusive = true }
                                }
                            },
                            onLoginClick = {
                                navController.navigate("login")
                            }
                        )
                    }
                    composable("profile") {
                        ProfileSetupScreen (
                            onProfileSaved = {
                                navController.navigate("home") {
                                    popUpTo("profile") { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}