package com.sahilmaske.peerlearn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sahilmaske.peerlearn.ui.Profile.ProfileSetupScreen
import com.sahilmaske.peerlearn.ui.auth.LoginScreen
import com.sahilmaske.peerlearn.ui.auth.RegisterScreen
import com.sahilmaske.peerlearn.ui.home.HomeScreenComponents.SeeAllPeersScreen
import com.sahilmaske.peerlearn.ui.home.NaviScreen
import com.sahilmaske.peerlearn.ui.home.ProfileScreen
import com.sahilmaske.peerlearn.ui.theme.PeerLearnTheme
import com.sahilmaske.peerlearn.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PeerLearnTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel()

                // Decide the start destination ONCE, not on every recomposition
                val startDestination = remember {
                    if (authViewModel.isUserLoggedIn) "home" else "login"
                }

                NavHost(
                    navController = navController,
                    startDestination = startDestination,
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
                        ProfileSetupScreen(
                            onProfileSaved = {
                                navController.navigate("home") {
                                    popUpTo("profile") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("home") {
                        NaviScreen(navController = navController)
                    }
                    composable("see_all_peers") {
                        SeeAllPeersScreen(navController = navController)
                    }
                    composable(
                        route = "profile/{uid}",
                        arguments = listOf(navArgument("uid") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val uid = backStackEntry.arguments?.getString("uid") ?: ""
                        ProfileScreen(uid = uid)
                    }
                }
            }
        }
    }
}