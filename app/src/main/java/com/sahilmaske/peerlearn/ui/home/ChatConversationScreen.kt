package com.sahilmaske.peerlearn.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.sahilmaske.peerlearn.model.Message
import com.sahilmaske.peerlearn.ui.theme.AppColors
import com.sahilmaske.peerlearn.viewmodel.ChatConversationViewModel
import com.sahilmaske.peerlearn.viewmodel.PeerInfo

// ---------- Screen size categories (Material3 standard breakpoints) ----------
enum class ScreenSize { COMPACT, MEDIUM, EXPANDED }

@Composable
fun rememberScreenSize(): ScreenSize {
    val widthDp = LocalConfiguration.current.screenWidthDp
    return when {
        widthDp < 600 -> ScreenSize.COMPACT   // phones
        widthDp < 840 -> ScreenSize.MEDIUM    // small tablets / foldables
        else -> ScreenSize.EXPANDED           // large tablets
    }
}

@Composable
fun ChatConversationScreen(
    chatId: String,
    onBack: () -> Unit
) {
    val viewModel: ChatConversationViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ChatConversationViewModel::class.java)) {
                    return ChatConversationViewModel(chatId) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    )

    val peerInfo by viewModel.peerInfo.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    ChatConversationContent(
        peerInfo = peerInfo,
        messages = messages,
        currentUserId = currentUserId,
        onBack = onBack,
        onSendMessage = { viewModel.sendMessage(it) }
    )
}

@Composable
fun ChatConversationContent(
    peerInfo: PeerInfo,
    messages: List<Message>,
    currentUserId: String?,
    onBack: () -> Unit,
    onSendMessage: (String) -> Unit
) {
    val screenSize = rememberScreenSize()
    val listState = rememberLazyListState()

    // Content ko bade screens pe center karke max width dena (tablet pe full-width chat ajeeb lagta hai)
    val contentMaxWidth = when (screenSize) {
        ScreenSize.COMPACT -> Dp.Unspecified
        ScreenSize.MEDIUM -> 600.dp
        ScreenSize.EXPANDED -> 720.dp
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .statusBarsPadding() // <-- root ko status bar ke neeche se start karata hai (edge-to-edge fix)
                .imePadding()
                .then(
                    if (contentMaxWidth != Dp.Unspecified) Modifier.widthIn(max = contentMaxWidth)
                    else Modifier.fillMaxWidth()
                )
                .fillMaxWidth()
        ) {
            ChatTopBar(
                peerInfo = peerInfo,
                screenSize = screenSize,
                onBack = onBack
            )

            HorizontalDivider()

            // ---------- Messages List ----------
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPaddingFor(screenSize)),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(messages) { message ->
                    val isMe = message.senderId == currentUserId
                    MessageBubble(text = message.text, isMe = isMe, screenSize = screenSize)
                }
            }
            LaunchedEffect(messages.size) {
                if (messages.isNotEmpty()) {
                    listState.animateScrollToItem(messages.size - 1)
                }
            }

            // ---------- Input Bar ----------
            MessageInputBar(
                onSendMessage = onSendMessage,
                horizontalPadding = horizontalPaddingFor(screenSize)
            )
        }
    }
}

// ---------- Top Bar (screen-size aware) ----------
@Composable
fun ChatTopBar(
    peerInfo: PeerInfo,
    screenSize: ScreenSize,
    onBack: () -> Unit
) {
    val avatarSize = when (screenSize) {
        ScreenSize.COMPACT -> 40.dp
        ScreenSize.MEDIUM -> 48.dp
        ScreenSize.EXPANDED -> 52.dp
    }
    val nameFontSize = when (screenSize) {
        ScreenSize.COMPACT -> 16.sp
        ScreenSize.MEDIUM -> 18.sp
        ScreenSize.EXPANDED -> 20.sp
    }
    val topBarPadding = horizontalPaddingFor(screenSize)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = topBarPadding, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBackIos,
                contentDescription = "Back",
                tint = AppColors.Icon
            )
        }

        if (peerInfo.avatarUrl.isBlank()) {
            // Photo nahi hai -> fallback placeholder (peach bg + swap-arrow icon)
            Box(
                modifier = Modifier
                    .size(avatarSize)
                    .clip(CircleShape)
                    .background(Color(0xFFFCE4CC)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.SwapHoriz,
                    contentDescription = "No profile photo",
                    tint = Color(0xFFB5651D),
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            AsyncImage(
                model = peerInfo.avatarUrl,
                contentDescription = peerInfo.name,
                modifier = Modifier
                    .size(avatarSize)
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // weight(1f) diya taaki name apna dedicated space le, avatar/icons ke beech squeeze na ho
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = peerInfo.name.ifBlank { "Unknown User" },
                fontWeight = FontWeight.SemiBold,
                fontSize = nameFontSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (screenSize != ScreenSize.COMPACT) {
                Text(
                    text = "Online",
                    fontSize = 12.sp,
                    color = AppColors.Primary
                )
            }
        }
    }
}

// ---------- Message Bubble ----------
@Composable
fun MessageBubble(text: String, isMe: Boolean, screenSize: ScreenSize) {
    val maxBubbleWidth = when (screenSize) {
        ScreenSize.COMPACT -> 260.dp
        ScreenSize.MEDIUM -> 380.dp
        ScreenSize.EXPANDED -> 460.dp
    }
    val fontSize = if (screenSize == ScreenSize.COMPACT) 14.sp else 15.sp

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = if (isMe) AppColors.Primary else Color(0xFFE8E8E8),
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isMe) 16.dp else 4.dp,
                        bottomEnd = if (isMe) 4.dp else 16.dp
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .widthIn(max = maxBubbleWidth)
        ) {
            Text(
                text = text,
                color = if (isMe) Color.White else Color.Black,
                fontSize = fontSize
            )
        }
    }
}

// ---------- Input Bar ----------
@Composable
fun MessageInputBar(onSendMessage: (String) -> Unit, horizontalPadding: Dp) {
    var text by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Type a message...") },
            shape = RoundedCornerShape(24.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = {
                if (text.isNotBlank()) {
                    onSendMessage(text.trim())
                    text = ""
                }
            }
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                tint = AppColors.Primary
            )
        }
    }
}

// ---------- Helper ----------
@Composable
fun horizontalPaddingFor(screenSize: ScreenSize) = when (screenSize) {
    ScreenSize.COMPACT -> 12.dp
    ScreenSize.MEDIUM -> 24.dp
    ScreenSize.EXPANDED -> 32.dp
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun ChatConversationScreenPreviewPhone() {
    ChatConversationContent(
        peerInfo = PeerInfo(name = "Sarah Jenkins", avatarUrl = ""),
        messages = listOf(
            Message(senderId = "other", text = "Hi! I saw your request for a swap 😊"),
            Message(senderId = "me", text = "That's awesome! I'd love that.")
        ),
        currentUserId = "me",
        onBack = {},
        onSendMessage = {}
    )
}

@Preview(showBackground = true, widthDp = 800)
@Composable
fun ChatConversationScreenPreviewTablet() {
    ChatConversationContent(
        peerInfo = PeerInfo(name = "Sarah Jenkins", avatarUrl = ""),
        messages = listOf(
            Message(senderId = "other", text = "Hi! I saw your request for a swap 😊"),
            Message(senderId = "me", text = "That's awesome! I'd love that.")
        ),
        currentUserId = "me",
        onBack = {},
        onSendMessage = {}
    )
}