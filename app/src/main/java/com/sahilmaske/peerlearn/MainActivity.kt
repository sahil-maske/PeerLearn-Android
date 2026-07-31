package com.sahilmaske.peerlearn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.sahilmaske.peerlearn.ui.Profile.ProfileSetupScreen
import com.sahilmaske.peerlearn.ui.auth.LoginScreen
import com.sahilmaske.peerlearn.ui.auth.RegisterScreen
import com.sahilmaske.peerlearn.ui.home.ChatConversationScreen
import com.sahilmaske.peerlearn.ui.home.HomeScreenComponents.SeeAllPeersScreen
import com.sahilmaske.peerlearn.ui.home.NaviScreen
import com.sahilmaske.peerlearn.ui.home.ProfileScreen
import com.sahilmaske.peerlearn.ui.notifications.NotificationScreen
import com.sahilmaske.peerlearn.ui.theme.PeerLearnTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PeerLearnTheme {
                PeerLearnApp()
            }
        }
    }
}

@Composable
fun PeerLearnApp() {
    val navController = rememberNavController()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val startDestination = if (currentUser == null) "login" else "main_nav"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("main_nav") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate("register") }
            )
        }
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("profile_setup") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onLoginClick = { navController.navigate("login") }
            )
        }
        composable("profile_setup") {
            ProfileSetupScreen(
                onProfileSaved = {
                    navController.navigate("main_nav") {
                        popUpTo("profile_setup") { inclusive = true }
                    }
                }
            )
        }
        composable("main_nav") {
            NaviScreen(navController = navController)
        }
        composable("notifications") {
            NotificationScreen(
                onNavigateToHome = { navController.popBackStack() }
            )
        }
        composable("see_all_peers") {
            SeeAllPeersScreen(navController = navController)
        }
        composable("profile/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            ProfileScreen(
                uid = userId,
                onNavigateToChat = { chatId ->
                    navController.navigate("chat_conversation/$chatId")
                }
            )
        }
        composable("chat_conversation/{chatId}") { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            ChatConversationScreen(
                chatId = chatId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
