@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.carepulse.app.ui.screens.customer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.carepulse.app.R
import com.carepulse.app.data.model.Caregiver
import com.carepulse.app.ui.components.CarePulseTextField
import com.carepulse.app.ui.components.GeneratedAvatar
import com.carepulse.app.ui.components.LoadingShimmerList
import com.carepulse.app.ui.components.PastelChip
import com.carepulse.app.ui.components.RatingRow
import com.carepulse.app.ui.theme.GlassScreen
import com.carepulse.app.ui.theme.Motion
import com.carepulse.app.ui.theme.Radii
import com.carepulse.app.ui.theme.Spacing
import com.carepulse.app.ui.theme.TypeNumericM
import com.carepulse.app.ui.theme.glassCard
import com.carepulse.app.viewmodel.CarePulseViewModel
import dev.chrisbanes.haze.HazeState
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

    GlassScreen { hazeState ->
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "Hello, ${displayName.split(" ").first()}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Find care today",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onSignOut) {
                            Icon(
                                Icons.Filled.Logout,
                                contentDescription = stringResource(R.string.auth_sign_out),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = onRequestCare,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Request care", style = MaterialTheme.typography.titleMedium) }
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = Spacing.ScreenPaddingCompact)
            ) {
                PulseBanner(hazeState = hazeState, onClick = onOpenPulse)

                Spacer(Modifier.height(Spacing.CardGap))

                Column(
                    Modifier
                        .fillMaxWidth()
                        .glassCard(radius = Radii.Card, hazeState = hazeState)
                        .padding(Spacing.CardPaddingCompact),
                    verticalArrangement = Arrangement.spacedBy(Spacing.InCardGap)
                ) {
                    CarePulseTextField(
                        value = area,
                        onValueChange = vm::setAreaFilter,
                        label = "Area or zip code",
                        placeholder = "e.g. 90210"
                    )

                    Text(
                        "Specialization",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    FlowRow(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PastelChip(
                            "All", selected = specFilter == null,
                            onClick = { vm.setSpecializationFilter(null) }
                        )
                        allSpecs.forEach {
                            PastelChip(
                                it, selected = specFilter == it,
                                onClick = { vm.setSpecializationFilter(it) }
                            )
                        }
                    }

                    Text(
                        "Minimum rating: ${"%.1f".format(minRating)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Slider(
                        value = minRating,
                        onValueChange = vm::setMinRating,
                        valueRange = 0f..5f,
                        steps = 9,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                Spacer(Modifier.height(Spacing.CardGap))

                if (loading) {
                    LoadingShimmerList()
                } else {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut()
                    ) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(Spacing.CardGap)) {
                            itemsIndexed(filtered, key = { _, c -> c.id }) { index, c ->
                                var visible by remember(c.id) { mutableStateOf(false) }
                                LaunchedEffect(c.id) {
                                    val step = minOf(index, Motion.ListStaggerMax) * Motion.ListStaggerStep.toLong()
                                    delay(step)
                                    visible = true
                                }
                                AnimatedVisibility(
                                    visible = visible,
                                    enter = slideInVertically(
                                        initialOffsetY = { (it * 0.06f).toInt() },
                                        animationSpec = tween(Motion.ListItemDuration, easing = Motion.Emphasized)
                                    ) +
                                        fadeIn(animationSpec = tween(Motion.ListItemDuration, easing = Motion.Emphasized)) +
                                        scaleIn(
                                            initialScale = 0.985f,
                                            animationSpec = tween(Motion.ListItemDuration, easing = Motion.Emphasized)
                                        )
                                ) {
                                    CaregiverCard(c, hazeState) { onOpenCaregiver(c.id) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PulseBanner(hazeState: HazeState, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .glassCard(radius = Radii.CardLarge, hazeState = hazeState)
            .clickable { onClick() }
            .padding(horizontal = Spacing.CardPaddingCompact),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                "The Pulse Dashboard",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Check today's vitals & video call",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun CaregiverCard(c: Caregiver, hazeState: HazeState, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(radius = Radii.Card, hazeState = hazeState)
            .clickable(onClick = onClick)
            .padding(Spacing.CardPaddingCompact)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GeneratedAvatar(
                seed = c.avatarSeed,
                initials = c.name.split(" ").map { it.first() }.joinToString(""),
                size = 64
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    c.name, style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.LocationOn, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        c.area, style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RatingRow(c.rating, c.ratingCount)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "\$${c.hourlyRate}/hr",
                        style = TypeNumericM,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    c.specializations.forEach { PastelChip(it, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)) }
                }
            }
        }
    }
}
