package com.sahilmaske.peerlearn.ui.components


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sahilmaske.peerlearn.ui.theme.AppColors

// Which set of actions the sheet should offer, based on the current connection
// state between the logged-in user and the profile being viewed.
enum class ConnectionActionMode { NONE, ACCEPTED, PENDING_SENT, UNBLOCK }

/**
 * Bottom sheet with the destructive connection actions: cancel a sent request,
 * break an accepted connection, block a user, or undo a block.
 *
 * Every destructive action goes through its own inline "are you sure" step
 * (confirmingAction) instead of firing immediately on tap — block and break
 * are hard to walk back, and cancel is annoying to redo by accident.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionActionsSheet(
    show: Boolean,
    mode: ConnectionActionMode,
    onDismiss: () -> Unit,
    onBreakConnection: () -> Unit = {},
    onCancelRequest: () -> Unit = {},
    onBlockUser: () -> Unit = {},
    onUnblockUser: () -> Unit = {}
) {
    if (!show || mode == ConnectionActionMode.NONE) return

    val sheetState = rememberModalBottomSheetState()
    // Which action (if any) is currently showing its confirm step. Reset every
    // time the sheet is freshly opened.
    var confirmingAction by remember(show) { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = {
            confirmingAction = null
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = AppColors.Surface
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {

            if (confirmingAction == null) {
                when (mode) {
                    ConnectionActionMode.ACCEPTED -> {
                        ActionRow(
                            icon = Icons.Default.HeartBroken,
                            label = "Break Connection",
                            color = AppColors.TextPrimary,
                            onClick = { confirmingAction = "break" }
                        )
                        ActionRow(
                            icon = Icons.Default.Block,
                            label = "Block User",
                            color = AppColors.Error,
                            onClick = { confirmingAction = "block" }
                        )
                    }
                    ConnectionActionMode.PENDING_SENT -> {
                        ActionRow(
                            icon = Icons.Default.Close,
                            label = "Cancel Request",
                            color = AppColors.TextPrimary,
                            onClick = { confirmingAction = "cancel" }
                        )
                        ActionRow(
                            icon = Icons.Default.Block,
                            label = "Block User",
                            color = AppColors.Error,
                            onClick = { confirmingAction = "block" }
                        )
                    }
                    ConnectionActionMode.UNBLOCK -> {
                        ActionRow(
                            icon = Icons.Default.LockOpen,
                            label = "Unblock User",
                            color = AppColors.TextPrimary,
                            onClick = { confirmingAction = "unblock" }
                        )
                    }
                    ConnectionActionMode.NONE -> {}
                }
                Spacer(Modifier.height(8.dp))
            } else {
                val (message, confirmLabel, action) = when (confirmingAction) {
                    "break" -> Triple(
                        "Break this connection? You'll need to reconnect to message each other again.",
                        "Break Connection",
                        onBreakConnection
                    )
                    "cancel" -> Triple(
                        "Cancel this connection request?",
                        "Cancel Request",
                        onCancelRequest
                    )
                    "block" -> Triple(
                        "Block this user? They won't be able to reconnect or message you, and any existing connection will be removed.",
                        "Block User",
                        onBlockUser
                    )
                    "unblock" -> Triple(
                        "Unblock this user? They'll be able to send you a connection request again.",
                        "Unblock User",
                        onUnblockUser
                    )
                    else -> Triple("", "", {})
                }

                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = AppColors.TextSecondary,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { confirmingAction = null },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Nevermind", color = AppColors.TextPrimary)
                    }
                    Button(
                        onClick = action,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (confirmingAction == "block") AppColors.Error else AppColors.Primary
                        )
                    ) {
                        Text(confirmLabel, color = AppColors.TextWhite)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ActionRow(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Text(label, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = color)
    }
}