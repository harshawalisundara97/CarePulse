@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.carepulse.app.ui.screens.activity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.carepulse.app.data.model.Booking
import com.carepulse.app.data.model.BookingStatus
import com.carepulse.app.ui.components.PastelCard
import com.carepulse.app.ui.components.PastelChip
import com.carepulse.app.ui.theme.Background
import com.carepulse.app.ui.theme.AccentPrimary
import com.carepulse.app.ui.theme.TextPrimary
import com.carepulse.app.ui.theme.TextSecondary
import com.carepulse.app.viewmodel.CarePulseViewModel

@Composable
fun ActivityScreen(
    vm: CarePulseViewModel,
    onRateCaregiver: (caregiverId: String, bookingId: String) -> Unit = { _, _ -> }
) {
    val bookings by vm.familyBookings.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Upcoming", "Past")

    val upcoming = bookings.filter {
        it.status == BookingStatus.CONFIRMED || it.status == BookingStatus.IN_PROGRESS
    }
    val past = bookings.filter { it.status == BookingStatus.COMPLETED }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "My Bookings",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Background
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = AccentPrimary
            ) {
                tabs.forEachIndexed { i, title ->
                    Tab(
                        selected = selectedTab == i,
                        onClick = { selectedTab = i },
                        text = { Text(title) }
                    )
                }
            }

            val list = if (selectedTab == 0) upcoming else past
            if (list.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No ${tabs[selectedTab].lowercase()} bookings.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item { Spacer(Modifier.height(8.dp)) }
                    items(list) { booking ->
                        BookingCard(booking, onRateCaregiver)
                    }
                }
            }
        }
    }
}

@Composable
private fun BookingCard(
    booking: Booking,
    onRate: (String, String) -> Unit
) {
    val statusColor = when (booking.status) {
        BookingStatus.CONFIRMED -> TextSecondary
        BookingStatus.IN_PROGRESS -> AccentPrimary
        BookingStatus.COMPLETED -> TextPrimary
    }
    PastelCard {
        Column {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        booking.patientName.ifBlank { "Patient" },
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "${booking.dateLabel} · ${booking.timeSlot}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                PastelChip(
                    booking.status.name.lowercase()
                        .replace("_", " ")
                        .replaceFirstChar { it.uppercase() }
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "LKR ${booking.totalCost}",
                style = MaterialTheme.typography.bodyMedium,
                color = statusColor,
                fontWeight = FontWeight.Medium
            )
            if (booking.status == BookingStatus.COMPLETED) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { onRate(booking.caregiverId, booking.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Rate caregiver")
                }
            }
        }
    }
}
