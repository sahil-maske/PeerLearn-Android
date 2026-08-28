package com.sahilmaske.peerlearn.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.sahilmaske.peerlearn.model.Message
import com.sahilmaske.peerlearn.model.User
import com.sahilmaske.peerlearn.ui.theme.AppColors
import com.sahilmaske.peerlearn.viewmodel.ChatConversationViewModel
import com.sahilmaske.peerlearn.viewmodel.PeerInfo
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun ChatConversationScreen(
    chatId: String,
    onBack: () -> Unit,
    onProfileClick: (String) -> Unit
) {
    val viewModel: ChatConversationViewModel = viewModel(
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
            items(messages) { message ->
                MessageBubble(
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

// ---------- Message Bubble (iMessage style) ----------
@Composable
fun MessageBubble(text: String, isMe: Boolean, timestamp: Long) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = if (isMe) AppColors.Primary else AppColors.Surface,
                    shape = RoundedCornerShape(18.dp)
                )
                .widthIn(max = 280.dp)
                .padding(horizontal = 14.dp, vertical = 9.dp)
        ) {
            Text(
                text = text,
                color = if (isMe) AppColors.TextWhite else AppColors.TextPrimary,
                fontSize = 15.sp
            )
        }
    }
}

// ---------- Input Bar (iOS style) ----------
@Composable
fun MessageInputBar(onSendMessage: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 44.dp),
            placeholder = { Text("Message", color = AppColors.TextSecondary) },
            shape = RoundedCornerShape(22.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF534AB7), // tera purple theme color
                unfocusedBorderColor = Color.Gray
            ),
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
                .size(38.dp)
                .clip(CircleShape)
                .background(if (text.isNotBlank()) AppColors.Primary else AppColors.Divider)
                .clickable(enabled = text.isNotBlank()) {
                    onSendMessage(text.trim())
                    text = ""
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                tint = AppColors.TextWhite,
                modifier = Modifier.size(18.dp)
            )
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
    ChatConversationContent(
        peerInfo = PeerInfo(name = "Sarah Jenkins", avatarUrl = ""),
        presence = User(isOnline = true), // NEW: preview ke liye dummy presence
        messages = listOf(
            Message(senderId = "other", text = "Hi! I saw your request for a swap 😊", timestamp = System.currentTimeMillis()),
            Message(senderId = "me", text = "That's awesome! I'd love that.", timestamp = System.currentTimeMillis())
        ),
        currentUserId = "me",
        onBack = {},
        onProfileClick = {},
        onSendMessage = {}
    )
}