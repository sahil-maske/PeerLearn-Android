package com.sahilmaske.peerlearn.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/**
 * Slide-to-confirm button, matching the reference design:
 * - Track: teal/green gradient, "Slide to request swap" label, circular thumb with swap icon
 * - On full drag: track turns black, label fades, checkmark shown (completion state)
 *
 * Usage:
 * SlideToSwapButton(
 *     onConfirmed = { /* fire actual swap-request Firestore call here */ }
 * )
 */
@Composable
fun SlideToSwapButton(
    modifier: Modifier = Modifier,
    label: String = "Slide to request swap",
    onConfirmed: () -> Unit
) {
    val trackHeight = 56.dp
    val thumbSize = 48.dp
    val density = LocalDensity.current

    var trackWidthPx by remember { mutableFloatStateOf(0f) }
    val thumbOffset = remember { Animatable(0f) }
    var confirmed by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val maxOffsetPx = remember(trackWidthPx) {
        with(density) { trackWidthPx - thumbSize.toPx() - 8f } // 8f = thumb inset padding
    }

    // Progress 0f..1f based on how far thumb has been dragged (used for visual fade only)
    val progress = if (maxOffsetPx > 0) (thumbOffset.value / maxOffsetPx).coerceIn(0f, 1f) else 0f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(trackHeight)
            .clip(RoundedCornerShape(50))
            .background(
                if (confirmed) {
                    Brush.horizontalGradient(listOf(Color(0xFF1A1A1A), Color(0xFF1A1A1A)))
                } else {
                    Brush.horizontalGradient(listOf(Color(0xFF0F6E6E), Color(0xFF1D9E75)))
                }
            )
            .onGloballyPositioned { coordinates ->
                trackWidthPx = coordinates.size.width.toFloat()
            },
        contentAlignment = Alignment.CenterStart
    ) {
        // Label — fades out as user drags, hidden once confirmed
        Text(
            text = if (confirmed) "" else label,
            color = Color.White.copy(alpha = (1f - progress) * 0.9f + 0.1f),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp),
            textAlign = TextAlign.Center
        )

        // Checkmark shown once confirmed
        if (confirmed) {
            Icon(
                Icons.Default.Check,
                contentDescription = "Confirmed",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(22.dp)
            )
        }

        // Draggable thumb with swap icon
        Box(
            modifier = Modifier
                .padding(4.dp)
                .offset { IntOffset(thumbOffset.value.toInt(), 0) }
                .size(thumbSize)
                .clip(CircleShape)
                .background(Color.White)
                .pointerInput(trackWidthPx) {
                    detectDragGestures(
                        onDragEnd = {
                            coroutineScope.launch {
                                // IMPORTANT: recompute progress here using the live thumbOffset.value.
                                // The outer `progress` val is captured once when this gesture lambda
                                // was first set up (pointerInput only restarts if trackWidthPx changes),
                                // so it stays stuck at its initial value (0f) and onConfirmed() would
                                // never fire no matter how far the user actually drags.
                                val liveProgress = if (maxOffsetPx > 0)
                                    (thumbOffset.value / maxOffsetPx).coerceIn(0f, 1f)
                                else 0f

                                if (liveProgress >= 0.85f) {
                                    // Snap to end, mark confirmed, fire callback
                                    thumbOffset.animateTo(maxOffsetPx, animationSpec = tween(150))
                                    confirmed = true
                                    onConfirmed()
                                } else {
                                    // Snap back to start
                                    thumbOffset.animateTo(0f, animationSpec = tween(250))
                                }
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            if (!confirmed) {
                                coroutineScope.launch {
                                    val newValue = (thumbOffset.value + dragAmount.x)
                                        .coerceIn(0f, maxOffsetPx.coerceAtLeast(0f))
                                    thumbOffset.snapTo(newValue)
                                }
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.SwapHoriz,
                contentDescription = null,
                tint = if (confirmed) Color(0xFF1A1A1A) else Color(0xFF0F6E6E),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}