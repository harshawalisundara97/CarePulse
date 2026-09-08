@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.carepulse.app.ui.screens.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.carepulse.app.R
import com.carepulse.app.ui.components.GeneratedAvatar
import com.carepulse.app.ui.components.PastelChip
import com.carepulse.app.ui.components.PrimaryButton
import com.carepulse.app.ui.components.RatingRow
import com.carepulse.app.ui.theme.AccentPrimary
import com.carepulse.app.ui.theme.GlassScreen
import com.carepulse.app.ui.theme.Radii
import com.carepulse.app.ui.theme.Spacing
import com.carepulse.app.ui.theme.glassCard
import com.carepulse.app.viewmodel.CarePulseViewModel
import dev.chrisbanes.haze.HazeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverDetailScreen(
    caregiverId: String,
    vm: CarePulseViewModel,
    onBook: () -> Unit,
    onBack: () -> Unit
) {
    val c = vm.caregiverById(caregiverId) ?: return

    var reviews by remember { mutableStateOf<List<com.carepulse.app.data.model.Review>>(emptyList()) }
    LaunchedEffect(caregiverId) {
        reviews = vm.reviewsFor(caregiverId)
    }

    GlassScreen { hazeState ->
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.common_back),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent,
            bottomBar = {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .glassCard(radius = Radii.CardLarge, hazeState = hazeState)
                        .padding(Spacing.ScreenPaddingCompact)
                ) {
                    PrimaryButton(text = "Book ${c.name.split(" ").first()}", onClick = onBook)
                }
            }
        ) { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.ScreenPaddingCompact),
                verticalArrangement = Arrangement.spacedBy(Spacing.CardGap)
            ) {
                // Accent-red profile header: hero avatar + stats row on a 2dp white-30%-alpha rule.
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                        .background(AccentPrimary)
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // GeneratedAvatar is a shared component that always clips to a circle;
                    // Radii.AvatarHero (24dp) has no effect layered on top of that and is
                    // intentionally not applied here -- see task report.
                    GeneratedAvatar(
                        seed = c.avatarSeed,
                        initials = c.name.split(" ").map { it.first() }.joinToString(""),
                        size = 96
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        c.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        c.area,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.height(20.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HeaderStat(title = "Rate", value = "\$${c.hourlyRate}/hr")
                        HeaderRule()
                        HeaderStat(title = "Reviews", value = "${c.ratingCount}")
                        HeaderRule()
                        HeaderStat(title = "Rating", value = "%.1f".format(c.rating))
                    }
                }

                Column(
                    Modifier
                        .fillMaxWidth()
                        .glassCard(radius = Radii.Card, hazeState = hazeState)
                        .padding(Spacing.CardPaddingCompact)
                ) {
                    Text(
                        "About",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        c.bio,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(
                    Modifier
                        .fillMaxWidth()
                        .glassCard(radius = Radii.Card, hazeState = hazeState)
                        .padding(Spacing.CardPaddingCompact)
                ) {
                    Text(
                        "Qualifications",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(6.dp))
                    // A single, quiet verification cue per qualification -- no stacked trust badges.
                    c.qualifications.forEach { q ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = AccentPrimary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(q, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        c.specializations.forEach { PastelChip(it, color = AccentPrimary.copy(alpha = 0.14f)) }
                    }
                }

                Column(
                    Modifier
                        .fillMaxWidth()
                        .glassCard(radius = Radii.Card, hazeState = hazeState)
                        .padding(Spacing.CardPaddingCompact)
                ) {
                    Text(
                        "Availability",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    // 7-day strip: AM/PM cells, each a Radii.Chip-shaped pill filled when available.
                    AvailabilityCalendar(c.availability)
                }

                if (reviews.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Reviews",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    reviews.forEach { review ->
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .glassCard(radius = Radii.Card, hazeState = hazeState)
                                .padding(Spacing.CardPaddingCompact)
                        ) {
                            RatingRow(review.rating, 0)
                            if (review.text.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    review.text,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                "— ${review.reviewerName}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(Modifier.height(60.dp))
            }
        }
    }
}

@Composable
private fun HeaderStat(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onPrimary
        )
        Text(
            title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
private fun HeaderRule() {
    Box(
        Modifier
            .width(2.dp)
            .height(28.dp)
            .background(Color.White.copy(alpha = 0.3f))
    )
}

@Composable
private fun AvailabilityCalendar(availability: List<String>) {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val slots = listOf("AM", "PM")
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row {
            Spacer(Modifier.width(36.dp))
            days.forEach { d ->
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(d, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        slots.forEach { slot ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(36.dp)) {
                    Text(slot, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                days.forEach { d ->
                    val available = availability.any { it.startsWith(d) && it.contains(slot) } ||
                        availability.contains(d)
                    Box(
                        Modifier
                            .weight(1f)
                            .padding(2.dp)
                            .height(28.dp)
                            .clip(RoundedCornerShape(Radii.Chip))
                            .background(if (available) AccentPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                    )
                }
            }
        }
    }
}
