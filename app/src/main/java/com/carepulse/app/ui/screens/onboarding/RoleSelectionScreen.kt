package com.carepulse.app.ui.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.carepulse.app.data.model.UserRole
import com.carepulse.app.ui.theme.CreamBackground
import com.carepulse.app.ui.theme.InkPrimary
import com.carepulse.app.ui.theme.InkSecondary
import com.carepulse.app.ui.theme.PastelMint
import com.carepulse.app.ui.theme.SoftLavender
import com.carepulse.app.ui.theme.SoftPeach

@Composable
fun RoleSelectionScreen(onRoleSelected: (UserRole) -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(CreamBackground, PastelMint.copy(alpha = 0.25f)))
            )
            .padding(24.dp)
    ) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Brand
            Box(
                Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(PastelMint, SoftLavender))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                "CarePulse",
                style = MaterialTheme.typography.displaySmall,
                color = InkPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Care, connected.",
                style = MaterialTheme.typography.bodyLarge,
                color = InkSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(48.dp))

            AnimatedVisibility(visible, enter = fadeIn() + slideInVertically(), exit = fadeOut()) {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    RoleCard(
                        icon = Icons.Filled.Favorite,
                        title = "I'm Family",
                        subtitle = "Find caregivers and stay connected with your loved ones.",
                        gradient = listOf(PastelMint, Color(0xFFC8F2DA)),
                        onClick = { onRoleSelected(UserRole.CUSTOMER) }
                    )
                    RoleCard(
                        icon = Icons.Filled.MedicalServices,
                        title = "I'm a Caregiver",
                        subtitle = "Offer your care services and grow your practice.",
                        gradient = listOf(SoftLavender, SoftPeach),
                        onClick = { onRoleSelected(UserRole.CAREGIVER) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RoleCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    gradient: List<Color>,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent
    ) {
        Box(
            Modifier
                .background(Brush.horizontalGradient(gradient))
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = InkPrimary, modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleLarge, color = InkPrimary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(2.dp))
                    Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = InkPrimary.copy(alpha = 0.75f))
                }
                Icon(Icons.Filled.ChevronRight, null, tint = InkPrimary)
            }
        }
    }
}
