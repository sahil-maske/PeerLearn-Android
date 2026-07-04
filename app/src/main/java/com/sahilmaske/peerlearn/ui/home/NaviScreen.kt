package com.sahilmaske.peerlearn.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.sahilmaske.peerlearn.ui.theme.AppColors
import com.sahilmaske.peerlearn.viewmodel.FeedViewModel
import com.sahilmaske.peerlearn.viewmodel.ProfileViewModel

@Composable
fun NaviScreen(
    profileViewModel: ProfileViewModel = viewModel(),
    feedViewModel: FeedViewModel = viewModel(
        factory = FeedViewModel.provideFactory(profileViewModel)
    )
) {
    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf("Feed", "QA", "Chat", "Profile")
    val icons = listOf(
        Icons.Default.Home,
        Icons.Default.QuestionAnswer,
        Icons.AutoMirrored.Filled.Chat,
        Icons.Default.Person
    )

    val userProfile by profileViewModel.userProfile.collectAsState()

    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            profileViewModel.fetchUserProfile(uid)
        }
    }

    // Root Box — isme content aur nav dono OVERLAP kar sakte hain (Column mein nahi hota)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {

        // ---- CONTENT: poori screen ki height leta hai, bottom tak jaata hai ----
        AnimatedContent(
            targetState = selectedItem,
            transitionSpec = {
                fadeIn(tween(200)) togetherWith fadeOut(tween(200))
            },
            label = "screenTransition",
            modifier = Modifier.fillMaxSize()
        ) { screen ->
            when (screen) {
                0 -> HomeScreen(viewModel = feedViewModel)
                1 -> QAScreen()
                2 -> ChatScreen()
                3 -> ProfileScreen(viewModel = profileViewModel)
            }
        }

        // ---- NAV BAR: content ke UPAR floating, bottom-center align ----
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 35.dp, vertical = 18.dp)
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
    var itemWidth by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current

    val indicatorOffset by animateDpAsState(
        targetValue = with(density) { (itemWidth * selectedItem).toDp() },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "indicatorOffset"
    )

    // Pill — TRANSPARENT background, taaki peeche ka scroll content dikhe
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White) // ⭐ transparent pill
            .height(64.dp)
    ) {
        // Sliding selected-tab indicator
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(with(density) { itemWidth.toDp() })
                .fillMaxHeight()
                .padding(8.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(AppColors.Primary.copy(alpha = 0.15f))
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    itemWidth = coordinates.size.width / items.size
                }
        ) {
            items.forEachIndexed { index, item ->
                val selected = selectedItem == index
                val animatedTint by animateColorAsState(
                    targetValue = if (selected) AppColors.Primary else AppColors.IconVariant,
                    label = "tint"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onItemSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icons[index],
                        contentDescription = item,
                        tint = animatedTint,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {

        NaviScreen()
}
