package com.sahilmaske.peerlearn.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sahilmaske.peerlearn.ui.theme.AppColors


@Composable
fun PostScreen(

)

{
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .background(AppColors.Surface.copy(alpha = 0.5f),
                    RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {

            IconButton(
                onClick = {},
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = AppColors.Icon
                )
            }
            Text(
                text = "Create Post",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = AppColors.TextPrimary,
                textAlign = TextAlign.Center
            )

            IconButton(
                onClick = {},
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    Icons.Default.School,
                    contentDescription = "School",
                    tint = AppColors.Icon
                )

        }

    }

}
}

@Preview(showBackground = true)
@Composable
fun PostScreenPreview() {
    PostScreen()
}
