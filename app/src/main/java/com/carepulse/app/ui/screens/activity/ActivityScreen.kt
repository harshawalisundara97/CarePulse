@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.carepulse.app.ui.screens.activity

import com.carepulse.app.ui.theme.tabContentWindowInsets
import com.carepulse.app.ui.theme.withoutBottom
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import com.carepulse.app.data.model.Booking
import com.carepulse.app.data.model.BookingStatus
import com.carepulse.app.ui.theme.GlassScreen
import com.carepulse.app.ui.theme.Radii
import com.carepulse.app.ui.theme.Spacing
import com.carepulse.app.ui.theme.StatusAvailable
import com.carepulse.app.ui.theme.StatusOnDuty
import com.carepulse.app.ui.theme.StatusOnDutyText
import com.carepulse.app.ui.theme.glassCard
import com.carepulse.app.viewmodel.CarePulseViewModel
import dev.chrisbanes.haze.HazeState
import java.text.NumberFormat
import java.util.Locale

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

    GlassScreen { hazeState ->
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "My Bookings",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            contentWindowInsets = tabContentWindowInsets(),
            containerColor = Color.Transparent
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding.withoutBottom())) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary
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
                    Box(
                        Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding()),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No ${tabs[selectedTab].lowercase()} bookings.",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    LazyColumn(
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = Spacing.ScreenPaddingCompact),
                        verticalArrangement = Arrangement.spacedBy(Spacing.CardGap),
                        contentPadding = PaddingValues(bottom = padding.calculateBottomPadding())
                    ) {
                        item { Spacer(Modifier.height(8.dp)) }
                        items(list) { booking ->
                            BookingCard(booking, onRateCaregiver, hazeState)
                        }
                    }
                }
            }
        }
    }
}

/** Fill/text colour pair for a [BookingStatus] pill: 16%-alpha fill of the hue, full-strength text. */
@Composable
private fun statusPillColors(status: BookingStatus): Pair<Color, Color> = when (status) {
    BookingStatus.CONFIRMED -> StatusAvailable.copy(alpha = 0.16f) to StatusAvailable
    BookingStatus.IN_PROGRESS -> StatusOnDuty.copy(alpha = 0.16f) to StatusOnDutyText
    BookingStatus.COMPLETED -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f) to MaterialTheme.colorScheme.onSurface
}

@Composable
private fun StatusPill(label: String, fill: Color, textColor: Color) {
    Box(
        Modifier
            .background(fill, RoundedCornerShape(Radii.Chip))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = textColor
        )
    }
}

@Composable
private fun BookingCard(
    booking: Booking,
    onRate: (String, String) -> Unit,
    hazeState: HazeState
) {
    val label = booking.status.name.lowercase()
        .replace("_", " ")
        .replaceFirstChar { it.uppercase() }

    val (fill, textColor) = statusPillColors(booking.status)

    val formattedTotal = "LKR " + NumberFormat.getNumberInstance(Locale.US).format(booking.totalCost)

    Box(
        Modifier
            .fillMaxWidth()
            .glassCard(radius = Radii.Card, hazeState = hazeState)
            .padding(Spacing.CardPaddingCompact)
    ) {
        Column {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        booking.patientName.ifBlank { "Patient" },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "${booking.dateLabel} · ${booking.timeSlot}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                StatusPill(label = label, fill = fill, textColor = textColor)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                formattedTotal,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
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
