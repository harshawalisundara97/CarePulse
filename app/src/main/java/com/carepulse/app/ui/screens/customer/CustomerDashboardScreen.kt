@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.carepulse.app.ui.screens.customer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.carepulse.app.data.model.Caregiver
import com.carepulse.app.ui.components.CarePulseTextField
import com.carepulse.app.ui.components.GeneratedAvatar
import com.carepulse.app.ui.components.LoadingShimmerList
import com.carepulse.app.ui.components.PastelChip
import com.carepulse.app.ui.components.RatingRow
import com.carepulse.app.ui.theme.Background
import com.carepulse.app.ui.theme.BorderLine
import com.carepulse.app.ui.theme.TealAccent
import com.carepulse.app.ui.theme.TealLight
import com.carepulse.app.ui.theme.TextPrimary
import com.carepulse.app.ui.theme.TextSecondary
import com.carepulse.app.viewmodel.CarePulseViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDashboardScreen(
    vm: CarePulseViewModel,
    onOpenCaregiver: (String) -> Unit,
    onOpenPulse: () -> Unit,
    onRequestCare: () -> Unit = {},
    onSignOut: () -> Unit = {}
) {
    val caregivers by vm.caregivers.collectAsState()
    val displayName by vm.displayName.collectAsState()
    val area by vm.areaFilter.collectAsState()
    val minRating by vm.minRating.collectAsState()
    val specFilter by vm.specializationFilter.collectAsState()

    var loading by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) { delay(700); loading = false }

    val allSpecs = remember(caregivers) {
        caregivers.flatMap { it.specializations }.distinct()
    }

    val filtered = caregivers.filter {
        (area.isBlank() || it.area.contains(area, ignoreCase = true)) &&
            it.rating >= minRating &&
            (specFilter == null || it.specializations.contains(specFilter))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Hello, ${displayName.split(" ").first()}",
                            style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        Text("Find care today",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(onClick = onSignOut) {
                        Icon(Icons.Filled.Logout, contentDescription = "Sign out", tint = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onRequestCare,
                containerColor = TealAccent,
                contentColor = Color.White,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Request care") }
            )
        },
        containerColor = Background
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            PulseBanner(onClick = onOpenPulse)

            Spacer(Modifier.height(16.dp))
            CarePulseTextField(
                value = area,
                onValueChange = vm::setAreaFilter,
                label = "Area or zip code",
                placeholder = "e.g. 90210"
            )

            Spacer(Modifier.height(10.dp))
            Text("Specialization", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            FlowRow(
                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PastelChip("All", selected = specFilter == null,
                    onClick = { vm.setSpecializationFilter(null) })
                allSpecs.forEach {
                    PastelChip(it, selected = specFilter == it,
                        onClick = { vm.setSpecializationFilter(it) })
                }
            }

            Spacer(Modifier.height(8.dp))
            Text("Minimum rating: ${"%.1f".format(minRating)}",
                style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Slider(
                value = minRating,
                onValueChange = vm::setMinRating,
                valueRange = 0f..5f,
                steps = 9
            )

            if (loading) {
                LoadingShimmerList()
            } else {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut()
                ) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        itemsIndexed(filtered, key = { _, c -> c.id }) { index, c ->
                            var visible by remember(c.id) { mutableStateOf(false) }
                            LaunchedEffect(c.id) {
                                delay(index * 60L)
                                visible = true
                            }
                            AnimatedVisibility(
                                visible = visible,
                                enter = slideInVertically(initialOffsetY = { it / 3 }, animationSpec = tween(350)) +
                                        fadeIn(animationSpec = tween(350))
                            ) {
                                CaregiverCard(c) { onOpenCaregiver(c.id) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PulseBanner(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(TealLight)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Favorite, null, tint = TextPrimary)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text("The Pulse Dashboard",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary, fontWeight = FontWeight.Bold)
            Text("Check today's vitals & video call",
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary.copy(alpha = 0.75f))
        }
    }
}

@Composable
private fun CaregiverCard(c: Caregiver, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GeneratedAvatar(
                seed = c.avatarSeed,
                initials = c.name.split(" ").map { it.first() }.joinToString(""),
                size = 64
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(c.name, style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, null, tint = TextSecondary,
                        modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(c.area, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RatingRow(c.rating, c.ratingCount)
                    Spacer(Modifier.width(8.dp))
                    Text("· \$${c.hourlyRate}/hr",
                        style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                }
                Spacer(Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    c.specializations.forEach { PastelChip(it, color = BorderLine) }
                }
            }
        }
    }
}
