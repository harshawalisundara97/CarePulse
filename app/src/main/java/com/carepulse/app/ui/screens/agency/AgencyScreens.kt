@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.carepulse.app.ui.screens.agency

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
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Payments
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.carepulse.app.ui.components.GeneratedAvatar
import com.carepulse.app.ui.components.PastelCard
import com.carepulse.app.ui.components.PastelChip
import com.carepulse.app.ui.theme.CreamBackground
import com.carepulse.app.ui.theme.InkPrimary
import com.carepulse.app.ui.theme.InkSecondary
import com.carepulse.app.ui.theme.PastelMintDeep
import com.carepulse.app.viewmodel.CarePulseViewModel

/** Agency admin home: company name + at-a-glance roster/request stats. */
@Composable
fun AgencyDashboardScreen(vm: CarePulseViewModel) {
    val profile by vm.profile.collectAsState()
    val caregivers by vm.caregivers.collectAsState()
    val bookings by vm.bookings.collectAsState()
    val companyName = profile?.displayName?.ifBlank { "Your agency" } ?: "Your agency"

    AgencyScaffold("Dashboard") { mod ->
        Column(mod, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Welcome back", style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
            Text(companyName, style = MaterialTheme.typography.headlineMedium,
                color = InkPrimary, fontWeight = FontWeight.Bold)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(Modifier.weight(1f), Icons.Filled.Groups,
                    "${caregivers.size}", "Caregivers")
                StatCard(Modifier.weight(1f), Icons.Filled.Inbox,
                    "0", "Open requests")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(Modifier.weight(1f), Icons.Filled.Payments,
                    "${bookings.size}", "Bookings")
                StatCard(Modifier.weight(1f), Icons.Filled.Groups,
                    "0", "On duty")
            }

            Spacer(Modifier.height(4.dp))
            Text("Quick start", style = MaterialTheme.typography.titleMedium,
                color = InkPrimary, fontWeight = FontWeight.SemiBold)
            PastelCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("1. Add your caregivers in the Caregivers tab.",
                        style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
                    Text("2. Families request care and you assign a match.",
                        style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
                    Text("3. Track bookings and payments in Billing.",
                        style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
                }
            }
        }
    }
}

/** Agency caregiver roster. */
@Composable
fun AgencyCaregiversScreen(vm: CarePulseViewModel) {
    val caregivers by vm.caregivers.collectAsState()
    AgencyScaffold("Caregivers") { mod ->
        if (caregivers.isEmpty()) {
            EmptyState(Icons.Filled.Groups, "No caregivers yet",
                "Add caregivers to your roster to start assigning them.")
        } else {
            LazyColumn(mod, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(caregivers) { cg ->
                    PastelCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            GeneratedAvatar(
                                seed = cg.avatarSeed,
                                initials = cg.name.split(" ")
                                    .mapNotNull { it.firstOrNull()?.toString() }
                                    .take(2).joinToString(""),
                                size = 48
                            )
                            Spacer(Modifier.size(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(cg.name, style = MaterialTheme.typography.titleMedium,
                                    color = InkPrimary, fontWeight = FontWeight.SemiBold)
                                Text(cg.area, style = MaterialTheme.typography.bodyMedium,
                                    color = InkSecondary)
                            }
                            PastelChip("\$${cg.hourlyRate}/hr")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AgencyRequestsScreen(vm: CarePulseViewModel) {
    AgencyScaffold("Requests") {
        EmptyState(Icons.Filled.Inbox, "No care requests yet",
            "When families request a caregiver, they'll appear here for you to assign.")
    }
}

@Composable
fun AgencyBillingScreen(vm: CarePulseViewModel) {
    AgencyScaffold("Billing") {
        EmptyState(Icons.Filled.Payments, "No billing yet",
            "Booking costs and payments will be tracked here.")
    }
}

// ---- Shared bits -----------------------------------------------------------

@Composable
private fun AgencyScaffold(title: String, content: @Composable (Modifier) -> Unit) {
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
        content(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun StatCard(modifier: Modifier, icon: ImageVector, value: String, label: String) {
    PastelCard(modifier) {
        Column {
            Icon(icon, null, tint = PastelMintDeep, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium,
                color = InkPrimary, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
        }
    }
}

@Composable
private fun EmptyState(icon: ImageVector, title: String, subtitle: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(72.dp).clip(CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = PastelMintDeep, modifier = Modifier.size(40.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.titleMedium,
                color = InkPrimary, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium,
                color = InkSecondary)
        }
    }
}
