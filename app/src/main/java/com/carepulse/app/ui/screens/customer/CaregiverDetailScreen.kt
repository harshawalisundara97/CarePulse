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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.carepulse.app.ui.components.GeneratedAvatar
import com.carepulse.app.ui.components.PastelCard
import com.carepulse.app.ui.components.PastelChip
import com.carepulse.app.ui.components.PrimaryButton
import com.carepulse.app.ui.components.RatingRow
import com.carepulse.app.ui.theme.CreamBackground
import com.carepulse.app.ui.theme.InkPrimary
import com.carepulse.app.ui.theme.InkSecondary
import com.carepulse.app.ui.theme.PastelMint
import com.carepulse.app.ui.theme.SoftLavender
import com.carepulse.app.ui.theme.SoftPeach
import com.carepulse.app.viewmodel.CarePulseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverDetailScreen(
    caregiverId: String,
    vm: CarePulseViewModel,
    onBook: () -> Unit,
    onBack: () -> Unit
) {
    val c = vm.caregiverById(caregiverId) ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = InkPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = CreamBackground,
        bottomBar = {
            Box(Modifier.padding(20.dp)) {
                PrimaryButton(text = "Book ${c.name.split(" ").first()}", onClick = onBook)
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Hero
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.horizontalGradient(listOf(PastelMint, SoftLavender))),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    GeneratedAvatar(
                        seed = c.avatarSeed,
                        initials = c.name.split(" ").map { it.first() }.joinToString(""),
                        size = 96
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(c.name, style = MaterialTheme.typography.titleLarge,
                        color = InkPrimary, fontWeight = FontWeight.Bold)
                    Text(c.area, style = MaterialTheme.typography.bodyMedium, color = InkPrimary.copy(alpha = 0.75f))
                }
            }

            // Stats row
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile(Modifier.weight(1f), title = "Rate", value = "\$${c.hourlyRate}/hr", color = SoftPeach)
                StatTile(Modifier.weight(1f), title = "Reviews", value = "${c.ratingCount}", color = SoftLavender)
                StatTile(Modifier.weight(1f), title = "Rating", value = "%.1f".format(c.rating), color = PastelMint)
            }

            PastelCard {
                Column {
                    Text("About", style = MaterialTheme.typography.titleMedium, color = InkPrimary, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text(c.bio, style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
                }
            }

            PastelCard {
                Column {
                    Text("Qualifications", style = MaterialTheme.typography.titleMedium, color = InkPrimary, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    c.qualifications.forEach { q ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                            Icon(Icons.Filled.CheckCircle, null, tint = PastelMint, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(q, style = MaterialTheme.typography.bodyMedium, color = InkPrimary)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        c.specializations.forEach { PastelChip(it, color = SoftPeach) }
                    }
                }
            }

            PastelCard {
                Column {
                    Text("Availability", style = MaterialTheme.typography.titleMedium, color = InkPrimary, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    // Mini calendar layout: 7 columns for next week, rows for AM / PM
                    AvailabilityCalendar(c.availability)
                }
            }

            Spacer(Modifier.height(60.dp))
        }
    }
}

@Composable
private fun StatTile(modifier: Modifier = Modifier, title: String, value: String, color: Color) {
    Box(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(color)
            .padding(12.dp)
    ) {
        Column {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = InkPrimary.copy(alpha = 0.7f))
            Text(value, style = MaterialTheme.typography.titleLarge, color = InkPrimary, fontWeight = FontWeight.Bold)
        }
    }
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
                    Text(d, style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
                }
            }
        }
        slots.forEach { slot ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(36.dp)) {
                    Text(slot, style = MaterialTheme.typography.labelLarge, color = InkSecondary)
                }
                days.forEach { d ->
                    val key = "$d $slot"
                    val available = availability.any { it.startsWith(d) && it.contains(slot) } ||
                        availability.contains(d)
                    Box(
                        Modifier
                            .weight(1f)
                            .padding(2.dp)
                            .height(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (available) PastelMint else SoftLavender.copy(alpha = 0.35f))
                    )
                }
            }
        }
    }
}
