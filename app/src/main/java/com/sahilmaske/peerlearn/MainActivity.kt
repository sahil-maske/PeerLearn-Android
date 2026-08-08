package com.sahilmaske.peerlearn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.sahilmaske.peerlearn.ui.Profile.ProfileSetupScreen
import com.sahilmaske.peerlearn.ui.auth.LoginScreen
import com.sahilmaske.peerlearn.ui.auth.RegisterScreen
import com.sahilmaske.peerlearn.ui.home.ChatConversationScreen
import com.sahilmaske.peerlearn.ui.home.HelpDetailScreen
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
        composable(
            route = "main_nav?tab={tab}",
            arguments = listOf(navArgument("tab") {
                type = NavType.IntType
                defaultValue = 0
            })
        ) { backStackEntry ->
            val tab = backStackEntry.arguments?.getInt("tab") ?: 0
            NaviScreen(navController = navController, initialTab = tab)
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
                },
                onBack = { navController.popBackStack() }
            )
        }
        // NOTE: route ek hi baar define hai (pehle duplicate tha, hata diya)
        composable("chat_conversation/{chatId}") { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            ChatConversationScreen(
                chatId = chatId,
                onBack = {
                    navController.navigate("main_nav?tab=2") {
                        popUpTo("main_nav?tab=2") { inclusive = true }
                    }
                },
                onProfileClick = { userId ->
                    navController.navigate("profile/$userId")
                }
            )
        }
        // NEW: Need Help post detail — shows comments as help offers, lets the
        // post owner mark commenters as having helped
        composable("help_detail/{postId}") { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: ""
            HelpDetailScreen(
                postId = postId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}