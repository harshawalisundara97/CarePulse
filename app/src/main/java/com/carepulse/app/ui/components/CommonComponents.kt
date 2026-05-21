package com.carepulse.app.ui.components

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.carepulse.app.ui.theme.InkPrimary
import com.carepulse.app.ui.theme.InkSecondary
import com.carepulse.app.ui.theme.PastelMint
import com.carepulse.app.ui.theme.PastelMintDeep
import com.carepulse.app.ui.theme.SoftLavender
import com.carepulse.app.ui.theme.SoftPeach

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled,
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PastelMintDeep,
            contentColor = Color.White,
            disabledContainerColor = PastelMint.copy(alpha = 0.5f)
        )
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun CarePulseTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = ""
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedIndicatorColor = PastelMintDeep,
            unfocusedIndicatorColor = SoftLavender,
            focusedLabelColor = PastelMintDeep,
            unfocusedLabelColor = InkSecondary
        )
    )
}

@Composable
fun SectionHeader(title: String, subtitle: String? = null, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium, color = InkPrimary)
        if (subtitle != null) {
            Spacer(Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
        }
    }
}

@Composable
fun PastelCard(
    modifier: Modifier = Modifier,
    containerColor: Color = Color.White,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(Modifier.padding(16.dp)) { content() }
    }
}

@Composable
fun GeneratedAvatar(seed: Int, initials: String, size: Int = 56, modifier: Modifier = Modifier) {
    val palette = listOf(
        PastelMint, SoftLavender, SoftPeach,
        Color(0xFFB6E2FF), Color(0xFFFFE5A8), Color(0xFFC8F2DA)
    )
    val a = palette[Math.floorMod(seed, palette.size)]
    val b = palette[Math.floorMod(seed + 2, palette.size)]
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(a, b)))
            .border(2.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            initials.take(2).uppercase(),
            color = InkPrimary,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun RatingRow(rating: Float, count: Int, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Icon(Icons.Filled.Star, null, tint = Color(0xFFFFB74D), modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(
            "%.1f".format(rating),
            style = MaterialTheme.typography.labelLarge,
            color = InkPrimary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.width(4.dp))
        Text("($count)", style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
    }
}

@Composable
fun PastelChip(
    label: String,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    color: Color = SoftLavender
) {
    val bg = if (selected) PastelMint else color
    val base = Modifier
        .padding(2.dp)
        .clip(RoundedCornerShape(14.dp))
    val tappable = if (onClick != null) base.clickable { onClick() } else base
    Surface(shape = RoundedCornerShape(14.dp), color = bg, modifier = tappable) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            color = InkPrimary
        )
    }
}

@Composable
fun ShimmerBox(width: Int, height: Int, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(800)),
        label = "shimmerAlpha"
    )
    Box(
        modifier
            .size(width.dp, height.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SoftLavender.copy(alpha = alpha))
    )
}

@Composable
fun LoadingShimmerList() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(4) {
            ShimmerBox(width = 320, height = 96)
        }
    }
}

