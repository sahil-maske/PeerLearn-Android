package com.sahilmaske.peerlearn

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.sahilmaske.peerlearn.util.AppearancePreferences
import com.sahilmaske.peerlearn.util.ThemeMode
import com.sahilmaske.peerlearn.ui.profile.ProfileSetupScreen
import com.sahilmaske.peerlearn.ui.profile.EditProfileScreen
import com.sahilmaske.peerlearn.services.FCMService
import com.sahilmaske.peerlearn.ui.components.WebViewScreen
import com.sahilmaske.peerlearn.ui.settings.AccountScreen
import com.sahilmaske.peerlearn.ui.settings.BlockUsersScreen
import com.sahilmaske.peerlearn.ui.settings.ChangePasswordScreen
import com.sahilmaske.peerlearn.ui.settings.DataUsage
import com.sahilmaske.peerlearn.ui.settings.LinkAccounts
import com.sahilmaske.peerlearn.services.PresenceManager
import com.sahilmaske.peerlearn.ui.settings.PrivacyScreen
import com.sahilmaske.peerlearn.ui.settings.ProfileInfo
import com.sahilmaske.peerlearn.ui.settings.ProfileVisibilityScreen
import com.sahilmaske.peerlearn.ui.settings.ReportProblemScreen
import com.sahilmaske.peerlearn.ui.settings.ReportUserScreen
import com.sahilmaske.peerlearn.ui.settings.SettingsScreen
import com.sahilmaske.peerlearn.ui.settings.SupportScreen
import com.sahilmaske.peerlearn.ui.auth.VerifyEmailScreen
import com.sahilmaske.peerlearn.ui.auth.LoginScreen
import com.sahilmaske.peerlearn.ui.auth.RegisterScreen
import com.sahilmaske.peerlearn.ui.chat.ChatConversationScreen
import com.sahilmaske.peerlearn.ui.help.HelpDetailScreen
import com.sahilmaske.peerlearn.ui.connections.SeeAllPeersScreen
import com.sahilmaske.peerlearn.navigation.NaviScreen
import com.sahilmaske.peerlearn.ui.profile.ProfileScreen
import com.sahilmaske.peerlearn.ui.connections.NotificationScreen
import com.sahilmaske.peerlearn.ui.theme.PeerLearnTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // poori app ke process lifecycle ke saath PresenceManager register kiya
        ProcessLifecycleOwner.get().lifecycle.addObserver(PresenceManager())

        // Fetch and update FCM token if user is already logged in
        if (FirebaseAuth.getInstance().currentUser != null) {
            FCMService.updateCurrentToken()
        }

        setContent {
            val prefs = remember { AppearancePreferences(this) }
            val themeMode by prefs.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            
            val useDarkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            PeerLearnTheme(darkTheme = useDarkTheme) {
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
                onNavigateToHome = { navController.popBackStack() },
                onNavigateToChat = { chatId ->
                    navController.navigate("chat_conversation/$chatId")
                }
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
                onBack = { navController.popBackStack() },
                onSettingsClick = { navController.navigate("settings") },
                onEditProfileClick = { navController.navigate("edit_profile") }
            )
        }
        composable("edit_profile") {
            EditProfileScreen(
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
                onBack = { navController.popBackStack() },
                onNavigateToProfile = { userId ->
                    navController.navigate("profile/$userId")
                }
            )
        }

        composable("settings") {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onAccountClick = { navController.navigate("account") },
                onPrivacyClick = { navController.navigate("privacy") },
                onSupportClick = { navController.navigate("support") },
                onToSClick = {
                    val encodedUrl = Uri.encode("https://sahil-maske.github.io/PeerLearn-legal/terms.html")
                    navController.navigate("webview?url=$encodedUrl&title=Terms of Service")
                },
                onPrivacyPolicyClick = {
                    val encodedUrl = Uri.encode("https://sahil-maske.github.io/PeerLearn-legal/privacy.html")
                    navController.navigate("webview?url=$encodedUrl&title=Privacy Policy")
                },
                onLogoutClick = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate("login") {
                        popUpTo("main_nav") { inclusive = true }
                    }
                },
                onDeleteSuccess = {
                    navController.navigate("login") {
                        popUpTo("main_nav") { inclusive = true }
                    }
                }
            )
        }
        composable("support") {
            SupportScreen(
                onBack = { navController.popBackStack() },
                onReportProblemClick = { navController.navigate("report_problem") },
                onReportUserClick = { navController.navigate("report_user") },
                onToSClick = {
                    val encodedUrl = Uri.encode("https://sahil-maske.github.io/PeerLearn-legal/terms.html")
                    navController.navigate("webview?url=$encodedUrl&title=Terms of Service")
                },
                onPrivacyClick = {
                    val encodedUrl = Uri.encode("https://sahil-maske.github.io/PeerLearn-legal/privacy.html")
                    navController.navigate("webview?url=$encodedUrl&title=Privacy Policy")
                }
            )
        }
        composable("report_problem") {
            ReportProblemScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("report_user") {
            ReportUserScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("account") {
            AccountScreen(
                onBack = { navController.popBackStack() },
                onVerifyEmailClick = {
                    navController.navigate("verify_email")
                },
                onLinkAccountClick = {
                    navController.navigate("link_accounts")
                },
                onChangePasswordClick = {
                    navController.navigate("change_password")
                }
            )
        }
        composable("verify_email") {
            VerifyEmailScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(route = "change_password") {
            ChangePasswordScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("link_accounts") {
            LinkAccounts(
                onBack = { navController.popBackStack() }
            )
        }
        composable("privacy") {
            PrivacyScreen(
                onBack = { navController.popBackStack() },
                onProfileVisibilityClick = { navController.navigate("profile_visibility") },
                onBlockedUsersClick = {navController.navigate("blocked_users")},
                onDataUsageClick = {navController.navigate("data_usage")}
            )
        }
        composable("profile_visibility") {
            ProfileVisibilityScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("blocked_users") {
            BlockUsersScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("data_usage"){
            DataUsage (
                onBack = { navController.popBackStack()},
                onProfileInfoClick = {navController.navigate("profile_info")}
            )
        }
        composable("profile_info"){
            ProfileInfo(
                onBack = { navController.popBackStack()}
            )
        }

        composable(
            route = "webview?url={url}&title={title}",
            arguments = listOf(
                navArgument("url") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val url = backStackEntry.arguments?.getString("url") ?: ""
            val title = backStackEntry.arguments?.getString("title") ?: ""
            WebViewScreen(
                url = url,
                title = title,
                onBack = { navController.popBackStack() }
            )
        }


    }
}