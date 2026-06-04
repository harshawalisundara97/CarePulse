package com.carepulse.app.ui.screens.customer

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.carepulse.app.ui.components.GeneratedAvatar
import com.carepulse.app.ui.theme.BorderLine
import com.carepulse.app.ui.theme.TealLight
import kotlinx.coroutines.delay

@Composable
fun VideoCallScreen(onEndCall: () -> Unit) {
    var seconds by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) { delay(1000); seconds++ }
    }

    // Subtle "breathing" pulse on the avatar.
    val pulse by rememberInfiniteTransition(label = "p").animateFloat(
        initialValue = 0.95f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label = "pulseScale"
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF1F2A44), Color(0xFF3D4E6D))))
    ) {
        // "Remote" view
        Column(
            Modifier.fillMaxSize().padding(top = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier
                    .size((220 * pulse).dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(TealLight, BorderLine))),
                contentAlignment = Alignment.Center
            ) {
                GeneratedAvatar(seed = 1, initials = "ML", size = 180)
            }
            Spacer(Modifier.height(24.dp))
            Text("Mr. Lee", color = Color.White,
                style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(formatTime(seconds), color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.titleMedium)
        }

        // Self preview (bottom-right thumbnail)
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .padding(20.dp)
                .size(width = 110.dp, height = 150.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.linearGradient(listOf(BorderLine, TealLight))),
            contentAlignment = Alignment.Center
        ) {
            GeneratedAvatar(seed = 99, initials = "AC", size = 56)
        }

        // Controls
        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CallControl(icon = Icons.Filled.Mic, bg = Color.White.copy(alpha = 0.18f), onClick = {})
            CallControl(icon = Icons.Filled.Videocam, bg = Color.White.copy(alpha = 0.18f), onClick = {})
            CallControl(icon = Icons.Filled.Cameraswitch, bg = Color.White.copy(alpha = 0.18f), onClick = {})
            CallControl(icon = Icons.Filled.CallEnd, bg = Color(0xFFE57373), tint = Color.White, size = 72, onClick = onEndCall)
        }
    }
}

@Composable
private fun CallControl(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    bg: Color,
    tint: Color = Color.White,
    size: Int = 56,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(bg)
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size((size / 2).dp))
    }
}

private fun formatTime(s: Int): String {
    val m = s / 60
    val sec = s % 60
    return "%02d:%02d".format(m, sec)
}
