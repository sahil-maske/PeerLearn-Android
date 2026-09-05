package com.sahilmaske.peerlearn.ui.Settings


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sahilmaske.peerlearn.ui.theme.AppColors

private data class AnalyticsItem(
    val icon: ImageVector,
    val title: String,
    val description: String
)

private val analyticsItems = listOf(
    AnalyticsItem(
        icon = Icons.AutoMirrored.Filled.TrendingUp,
        title = "App Usage Patterns",
        description = "Screens visited, session length"
    ),
    AnalyticsItem(
        icon = Icons.Default.GpsFixed,
        title = "Matching Improvements",
        description = "Helps suggest better skill matches"
    ),
    AnalyticsItem(
        icon = Icons.Default.BugReport,
        title = "Crash & Performance Reports",
        description = "Helps us fix bugs faster"
    )
)

@Composable
fun Analytics(
    onBack: () -> Unit = {}
) {
    var appUsageEnabled by remember { mutableStateOf(true) }
    var matchingEnabled by remember { mutableStateOf(true) }
    var crashReportsEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // Top bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBackIos,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = "Usage Analytics",
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Header card - icon, title, subtitle
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = "Usage Analytics",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "How we use your activity data",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "This helps us improve matching quality and app performance — not to track you personally.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 28.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Toggleable list card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            AnalyticsToggleRow(
                item = analyticsItems[0],
                checked = appUsageEnabled,
                onCheckedChange = { appUsageEnabled = it }
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.background,
                thickness = 1.dp
            )
            AnalyticsToggleRow(
                item = analyticsItems[1],
                checked = matchingEnabled,
                onCheckedChange = { matchingEnabled = it }
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.background,
                thickness = 1.dp
            )
            AnalyticsToggleRow(
                item = analyticsItems[2],
                checked = crashReportsEnabled,
                onCheckedChange = { crashReportsEnabled = it }
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Turning these off may reduce match quality but won't affect core app functionality.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun AnalyticsToggleRow(
    item: AnalyticsItem,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.surface,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.surface,
                uncheckedTrackColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AnalyticsPreview() {
    MaterialTheme {
        Analytics(
            onBack = {}
        )
    }
}