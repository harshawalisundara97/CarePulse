@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.carepulse.app.ui.screens.activity

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.carepulse.app.data.model.UserRole
import com.carepulse.app.ui.components.PastelCard
import com.carepulse.app.ui.theme.CreamBackground
import com.carepulse.app.ui.theme.InkPrimary
import com.carepulse.app.ui.theme.InkSecondary
import com.carepulse.app.ui.theme.PastelMintDeep
import com.carepulse.app.ui.theme.SoftPeach
import com.carepulse.app.viewmodel.CarePulseViewModel

/**
 * Role-aware "Activity" tab: families see their bookings, caregivers see the
 * shift reports they have submitted.
 */
@Composable
fun ActivityScreen(vm: CarePulseViewModel) {
    val role by vm.role.collectAsState()
    val isCaregiver = role == UserRole.CAREGIVER
    val title = if (isCaregiver) "My reports" else "My bookings"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(title, style = MaterialTheme.typography.headlineMedium,
                        color = InkPrimary, fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = CreamBackground
    ) { padding ->
        val mod = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 16.dp)

        if (isCaregiver) CaregiverReports(vm, mod) else CustomerBookings(vm, mod)
    }
}

@Composable
private fun CustomerBookings(vm: CarePulseViewModel, modifier: Modifier) {
    val bookings by vm.bookings.collectAsState()
    if (bookings.isEmpty()) {
        EmptyState(modifier, "No bookings yet", "Book a caregiver from the Home tab.")
        return
    }
    LazyColumn(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(bookings) { b ->
            PastelCard {
                Column {
                    Text(b.patientName.ifBlank { "Care visit" },
                        style = MaterialTheme.typography.titleMedium,
                        color = InkPrimary, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Text("${b.dateLabel} · ${b.timeSlot}",
                        style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatusPill(b.status.name)
                        Text("$${b.totalCost}", style = MaterialTheme.typography.titleMedium,
                            color = PastelMintDeep, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CaregiverReports(vm: CarePulseViewModel, modifier: Modifier) {
    val reports by vm.reports.collectAsState()
    if (reports.isEmpty()) {
        EmptyState(modifier, "No reports yet", "Clock out from the Home tab to file your first shift report.")
        return
    }
    LazyColumn(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(reports) { r ->
            PastelCard {
                Column {
                    Text(r.dateLabel, style = MaterialTheme.typography.titleMedium,
                        color = InkPrimary, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Text(r.daySummary, style = MaterialTheme.typography.bodyMedium,
                        color = InkSecondary, maxLines = 3)
                    Spacer(Modifier.height(8.dp))
                    Text("${r.medicationsGiven.count { it.administered }} of " +
                        "${r.medicationsGiven.size} medications given",
                        style = MaterialTheme.typography.bodySmall, color = PastelMintDeep)
                }
            }
        }
    }
}

@Composable
private fun StatusPill(status: String) {
    Box(
        Modifier.clip(MaterialTheme.shapes.small).background(SoftPeach).padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(status.lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelMedium, color = InkPrimary)
    }
}

@Composable
private fun EmptyState(modifier: Modifier, title: String, subtitle: String) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(72.dp).clip(CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.EventNote, null, tint = PastelMintDeep,
                    modifier = Modifier.size(40.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.titleMedium,
                color = InkPrimary, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
        }
    }
}
