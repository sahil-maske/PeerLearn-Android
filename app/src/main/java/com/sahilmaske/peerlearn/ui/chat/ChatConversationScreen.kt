package com.sahilmaske.peerlearn.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.platform.LocalConfiguration
import com.sahilmaske.peerlearn.data.model.Message
import com.sahilmaske.peerlearn.data.model.User
import com.sahilmaske.peerlearn.ui.theme.AppColors
import com.sahilmaske.peerlearn.viewmodel.ChatConversationViewModel
import com.sahilmaske.peerlearn.viewmodel.PeerInfo
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

// ---------- iOS-style hardcoded colors for chat bubbles ----------
private val MyBubbleColor = Color(0xFF0B93F6)       // iOS iMessage blue
private val TheirBubbleColor = Color(0xFF0B93F6)    // near-black (dark gray) for received messages
private val MyBubbleTextColor = Color.White
private val TheirBubbleTextColor = Color.White

@Composable
fun ChatConversationScreen(
    chatId: String,
    onBack: () -> Unit,
    onProfileClick: (String) -> Unit
) {
    val viewModel: ChatConversationViewModel = viewModel(
        key = chatId,
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ChatConversationViewModel(chatId) as T
            }
        }
    )

    val peerInfo by viewModel.peerInfo.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val otherUserPresence by viewModel.otherUserPresence.collectAsState() // NEW
    val currentUserId = remember { mutableStateOf(FirebaseAuth.getInstance().currentUser?.uid) }

    DisposableEffect(Unit) {
        val auth = FirebaseAuth.getInstance()
        val listener = FirebaseAuth.AuthStateListener {
            currentUserId.value = it.currentUser?.uid
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    ChatConversationContent(
        peerInfo = peerInfo,
        presence = otherUserPresence, // NEW
        messages = messages,
        currentUserId = currentUserId.value,
        onBack = onBack,
        onProfileClick = { onProfileClick(peerInfo.uid) },
        onSendMessage = { viewModel.sendMessage(it) }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatConversationContent(
    peerInfo: PeerInfo,
    presence: User? = null, // NEW (default null taaki preview mein bhi chale)
    messages: List<Message>,
    currentUserId: String?,
    onBack: () -> Unit,
    onProfileClick: () -> Unit,
    onSendMessage: (String) -> Unit
) {
    val listState = rememberLazyListState()

    // Smooth scroll to bottom when new messages arrive OR when keyboard opens
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .statusBarsPadding()
            .imePadding()
    ) {
        ChatTopBar(
            peerInfo = peerInfo,
            presence = presence, // NEW
            onBack = onBack,
            onProfileClick = onProfileClick
        )
        HorizontalDivider(color = AppColors.Divider, thickness = 0.5.dp)

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            itemsIndexed(
                items = messages,
                key = { index, message -> "${message.timestamp}_$index" }
            ) { _, message ->
                MessageBubble(
                    modifier = Modifier.animateItem(
                        fadeInSpec = tween(300),
                        placementSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        fadeOutSpec = tween(300)
                    ),
                    text = message.text,
                    isMe = message.senderId == currentUserId,
                    timestamp = message.timestamp
                )
            }
        }

        MessageInputBar(onSendMessage = onSendMessage)
    }
}

// ---------- Top Bar (iOS style) ----------
@Composable
fun ChatTopBar(
    peerInfo: PeerInfo,
    presence: User? = null, // NEW
    onBack: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .clickable { onProfileClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBackIos,
                contentDescription = "Back",
                tint = AppColors.Primary
            )
        }
        Spacer(modifier = Modifier.width(8.dp))

        if (peerInfo.avatarUrl.isBlank()) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(AppColors.PrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = null,
                    tint = AppColors.Primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            AsyncImage(
                model = peerInfo.avatarUrl,
                contentDescription = peerInfo.name,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // NEW: Name + status ek Column mein (subtitle ke liye)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = peerInfo.name.ifBlank { "Unknown User" },
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val status = presenceStatusText(presence)
            if (status.isNotBlank()) {
                Text(
                    text = status,
                    fontSize = 12.sp,
                    color = if (presence?.isOnline == true) Color(0xFF0F6E6E) else Color.Gray
                )
            }
        }
    }
}

// ---------- Message Bubble (iOS iMessage style, hardcoded colors + shape) ----------
@Composable
fun MessageBubble(
    modifier: Modifier = Modifier,
    text: String,
    isMe: Boolean,
    timestamp: Long
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.9f,
        animationSpec = tween(durationMillis = 200),
        label = "bubbleScale"
    )

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val maxBubbleWidth = screenWidth * 0.75f

    // iOS-style asymmetric corners: the "tail" corner is less rounded than the other three
    val bubbleShape = if (isMe) {
        RoundedCornerShape(
            topStart = 18.dp,
            topEnd = 18.dp,
            bottomStart = 18.dp,
            bottomEnd = 4.dp
        )
    } else {
        RoundedCornerShape(
            topStart = 18.dp,
            topEnd = 18.dp,
            bottomStart = 4.dp,
            bottomEnd = 18.dp
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(200)) + slideInVertically(initialOffsetY = { it / 2 }),
            modifier = Modifier.scale(scale)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = if (isMe) MyBubbleColor else TheirBubbleColor, // hardcoded, theme-independent
                        shape = bubbleShape
                    )
                    .widthIn(max = maxBubbleWidth)
                    .padding(horizontal = 14.dp, vertical = 9.dp)
            ) {
                Text(
                    text = text,
                    color = if (isMe) MyBubbleTextColor else TheirBubbleTextColor, // always white on both
                    fontSize = 15.sp
                )
            }
        }
    }
}

// ---------- Input Bar (hardcoded light style, always — does not follow app theme) ----------
@Composable
fun MessageInputBar(onSendMessage: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val borderColor by animateColorAsState(
        targetValue = if (isFocused) Color(0xFF0F6E6E) else Color(0xFFE0E0E0),
        animationSpec = tween(200),
        label = "borderColor"
    )

    val shadowElevation by animateDpAsState(
        targetValue = if (isFocused) 4.dp else 1.dp,
        animationSpec = tween(200),
        label = "shadowElevation"
    )

    val sendButtonScale by animateFloatAsState(
        targetValue = if (text.isNotBlank()) 1.1f else 1f,
        animationSpec = tween(200),
        label = "sendButtonScale"
    )

    Surface(
        color = Color(0xFFFFFFFF), // hardcoded white — always, regardless of app theme
        tonalElevation = 0.dp,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 44.dp)
                    .shadow(shadowElevation, RoundedCornerShape(percent = 50))
                    .border(1.dp, borderColor, RoundedCornerShape(percent = 50)),
                placeholder = {
                    Text(
                        "Message",
                        color = Color(0xFF9E9E9E), // hardcoded medium gray placeholder
                        fontSize = 15.sp
                    )
                },
                shape = RoundedCornerShape(percent = 50),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color(0xFFF5F5F5), // hardcoded light gray fill
                    unfocusedContainerColor = Color(0xFFF5F5F5),
                    focusedTextColor = Color(0xFF1A1A1A), // hardcoded near-black text
                    unfocusedTextColor = Color(0xFF1A1A1A),
                    cursorColor = Color(0xFF0F6E6E)
                ),
                interactionSource = interactionSource,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onDone = {
                    if (text.isNotBlank()) {
                        onSendMessage(text.trim())
                        text = ""
                    }
                }),
                maxLines = 4
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .scale(sendButtonScale)
                    .clip(CircleShape)
                    .background(
                        if (text.isNotBlank()) Color(0xFF0F6E6E) // teal when active
                        else Color(0xFFE0E0E0) // light gray when empty
                    )
                    .clickable(enabled = text.isNotBlank()) {
                        onSendMessage(text.trim())
                        text = ""
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (text.isNotBlank()) Color.White else Color(0xFF9E9E9E),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(timestamp)
}

// NEW: presence ko readable status text mein convert karta hai
private fun presenceStatusText(presence: User?): String {
    if (presence == null) return ""
    if (presence.hideOnlineStatus) return ""
    if (presence.isOnline) return "Online"
    if (presence.lastSeen <= 0L) return ""

    val diffMillis = System.currentTimeMillis() - presence.lastSeen
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis)
    val hours = TimeUnit.MILLISECONDS.toHours(diffMillis)
    val days = TimeUnit.MILLISECONDS.toDays(diffMillis)

    return when {
        minutes < 1 -> "Last seen just now"
        minutes < 60 -> "Last seen $minutes min ago"
        hours < 24 -> "Last seen $hours hr ago"
        else -> "Last seen $days d ago"
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
fun ChatConversationScreenPreview() {
    val currentTime = System.currentTimeMillis()
    ChatConversationContent(
        peerInfo = PeerInfo(name = "Sarah Jenkins", avatarUrl = ""),
        presence = User(isOnline = true), // NEW: preview ke liye dummy presence
        messages = listOf(
            Message(senderId = "other", text = "Hi! I saw your request for a swap 😊", timestamp = currentTime),
            Message(senderId = "me", text = "That's awesome! I'd love that.", timestamp = currentTime + 100)
        ),
        currentUserId = "me",
        onBack = {},
        onProfileClick = {},
        onSendMessage = {}
    )
}