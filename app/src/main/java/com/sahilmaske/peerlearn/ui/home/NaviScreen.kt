package com.sahilmaske.peerlearn.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.twotone.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.sahilmaske.peerlearn.ui.notifications.NotificationScreen
import com.sahilmaske.peerlearn.ui.theme.AppColors
import com.sahilmaske.peerlearn.viewmodel.FeedViewModel
import com.sahilmaske.peerlearn.viewmodel.ProfileViewModel
import androidx.compose.ui.platform.LocalInspectionMode

@Composable
fun NaviScreen(
    navController: NavController,
    initialTab: Int = 0,
    profileViewModel: ProfileViewModel = viewModel(),
    feedViewModel: FeedViewModel = viewModel(
        factory = FeedViewModel.provideFactory(profileViewModel)
    )
) {
    var selectedItem by remember(initialTab) { mutableIntStateOf(initialTab) }
    // NEW: added "Alerts" tab (index 3) so NotificationScreen is actually reachable.
    // It was fully built but never wired into navigation before this.
    val items = listOf("Home", "Post", "Chat", "Profile")
    val icons = listOf(
        Icons.Default.Home,
        Icons.Default.PostAdd,
        Icons.AutoMirrored.Filled.Chat,
        Icons.Default.Person
    )

    val userProfile by profileViewModel.userProfile.collectAsState()

    val isPreview = LocalInspectionMode.current
    val currentUserId = remember {
        mutableStateOf(if (isPreview) "mock_user" else FirebaseAuth.getInstance().currentUser?.uid ?: "")
    }
    // Observe auth state changes to update currentUserId
    DisposableEffect(Unit) {
        if (!isPreview) {
            val auth = FirebaseAuth.getInstance()
            val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                currentUserId.value = firebaseAuth.currentUser?.uid ?: ""
            }
            auth.addAuthStateListener(listener)
            onDispose { auth.removeAuthStateListener(listener) }
        } else {
            onDispose {}
        }
    }

    LaunchedEffect(Unit) {
        if (isPreview) return@LaunchedEffect
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            profileViewModel.fetchUserProfile(uid)
        }
    }

    // ---- Scroll-based nav visibility ----
    // navOffset = 0f  -> fully visible
    // navOffset = 1f  -> fully hidden (slid down + faded out)
    val navOffset = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                coroutineScope.launch {
                    val target = (navOffset.value - delta / 300f).coerceIn(0f, 1f)
                    // delta < 0 => scrolling down (content moves up) => hide nav (target increases)
                    // delta > 0 => scrolling up => show nav (target decreases)
                    navOffset.snapTo(target)
                }
                return Offset.Zero
            }
        }
    }

    // Root Box — isme content aur nav dono OVERLAP kar sakte hain (Column mein nahi hota)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .nestedScroll(nestedScrollConnection) // FIX: enables scroll-direction detection for nav hide/show
    ) {

        // ---- CONTENT: poori screen ki height leta hai, bottom tak jaata hai ----
        AnimatedContent(
            targetState = selectedItem,
            transitionSpec = {
                val direction = if (targetState > initialState) 1 else -1
                (slideInHorizontally(
                    animationSpec = tween(280),
                    initialOffsetX = { fullWidth -> direction * fullWidth / 4 }
                ) + fadeIn(animationSpec = tween(280))) togetherWith
                        (slideOutHorizontally(
                            animationSpec = tween(280),
                            targetOffsetX = { fullWidth -> -direction * fullWidth / 4 }
                        ) + fadeOut(animationSpec = tween(220)))
            },
            label = "screenTransition",
            modifier = Modifier.fillMaxSize()
        ) { screen ->
            when (screen) {
                0 -> HomeScreen(viewModel = feedViewModel, navController = navController, currentUserId = currentUserId.value)
                1 -> PostScreen(
                    onClose = { selectedItem = 0 }
                )
                2 -> ChatScreen(navController = navController)

                3 -> ProfileScreen(viewModel = profileViewModel)
            }
        }

        // ---- NAV BAR: content ke UPAR floating, bottom-center align ----
        // FIX: slides down + fades out on scroll-down, slides back up on scroll-up
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 35.dp, vertical = 18.dp)
                .graphicsLayer {
                    translationY = navOffset.value * 150f // slides down out of view
                    alpha = 1f - navOffset.value           // fades out simultaneously
                }
        ) {
            AnimatedBottomNav(
                items = items,
                icons = icons,
                selectedItem = selectedItem,
                onItemSelected = { selectedItem = it }
            )
        }
    }
}

@Composable
fun AnimatedBottomNav(
    items: List<String>,
    icons: List<androidx.compose.ui.graphics.vector.ImageVector>,
    selectedItem: Int,
    onItemSelected: (Int) -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            // Base scrim — darkens whatever is behind it just enough for contrast
            .background(Color.Black.copy(alpha = 0.12f))
            // Glass gradient on top of the scrim — was 0.35f/0.15f, now stronger
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.55f),
                        Color.White.copy(alpha = 0.35f)
                    )
                )
            )
            .border(1.5.dp, Color.White.copy(alpha = 0.55f), RoundedCornerShape(28.dp)) // stronger glass edge highlight
            .height(64.dp)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val selected = selectedItem == index
                val animatedTint by animateColorAsState(
                    targetValue = if (selected) AppColors.Primary else Color(0xFF3A3A3A),
                    label = "tint"
                )
                // FIX: was fixed dp widths (104.dp/52.dp) which could overflow or
                // look inconsistent across screen sizes. Now uses animated weight
                // so the row ALWAYS fills exactly the available width, on any device.
                val tabWeight by animateFloatAsState(
                    targetValue = if (selected) 2.2f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "tabWeight"
                )
                Box(
                    modifier = Modifier
                        .weight(tabWeight)
                        .fillMaxHeight()
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (selected) Color.White.copy(alpha = 0.55f) else Color.Transparent
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onItemSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icons[index],
                            contentDescription = item,
                            tint = animatedTint,
                            modifier = Modifier.size(22.dp)
                        )
                        if (selected) {
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = item,
                                color = AppColors.Primary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── PREVIEW #1 (safe): sirf AnimatedBottomNav test karta hai ──
@Preview(showBackground = true)
@Composable
fun AnimatedBottomNavPreview() {
    var selected by remember { mutableIntStateOf(0) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.Background)
            .padding(24.dp)
    ) {
        AnimatedBottomNav(
            items = listOf("Home", "Post", "Chat","Profile"),
            icons = listOf(
                Icons.Default.Home,
                Icons.Rounded.AddCircle,
                Icons.AutoMirrored.Filled.Chat,
                Icons.Default.Notifications,
                Icons.Default.Person
            ),
            selectedItem = selected,
            onItemSelected = { selected = it }
        )
    }
}

// ── PREVIEW #2 (unsafe — reference only): poori NaviScreen ─────
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val navController = rememberNavController()
    NaviScreen(navController = navController)
}