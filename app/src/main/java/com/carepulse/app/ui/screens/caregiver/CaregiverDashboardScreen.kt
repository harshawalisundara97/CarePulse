package com.carepulse.app.ui.screens.caregiver

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.carepulse.app.ui.components.GeneratedAvatar
import com.carepulse.app.ui.components.PastelCard
import com.carepulse.app.ui.components.PrimaryButton
import com.carepulse.app.viewmodel.CarePulseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverDashboardScreen(
    vm: CarePulseViewModel,
    onClockOut: () -> Unit,
    onSignOut: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onLogVitals: () -> Unit = {}
) {
    val displayName by vm.displayName.collectAsState()
    val allBookings by vm.bookings.collectAsState()
    val profile by vm.profile.collectAsState()
    val myShifts = allBookings.filter {
        it.caregiverUid == profile?.uid &&
        (it.status == com.carepulse.app.data.model.BookingStatus.CONFIRMED ||
         it.status == com.carepulse.app.data.model.BookingStatus.IN_PROGRESS)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Good morning,", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(displayName, style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(onClick = onEditProfile) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit profile", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onSignOut) {
                        Icon(Icons.Filled.Logout, contentDescription = "Sign out", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onLogVitals,
                icon = { Icon(Icons.Filled.MonitorHeart, contentDescription = null) },
                text = { Text("Log Vitals") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Active shift card
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(18.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onPrimaryContainer)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Active shift",
                            color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.labelLarge)
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("Mr. Lee", style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocationOn, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Brookside Care · Room 204",
                            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.AccessTime, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("9:00 AM – 1:00 PM",
                            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    }
                }
            }

            // Quick stats
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Stat(Modifier.weight(1f), "Today", "1 shift", MaterialTheme.colorScheme.outline)
                Stat(Modifier.weight(1f), "This week", "12 hrs", MaterialTheme.colorScheme.primaryContainer)
                Stat(Modifier.weight(1f), "Earnings", "\$340", MaterialTheme.colorScheme.primary)
            }

            PastelCard {
                Column {
                    Text("Upcoming patients", style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(10.dp))
                    listOf("Mrs. Adler" to "Tomorrow, 9 AM", "Mr. Patel" to "Thu, 2 PM").forEach { (n, t) ->
                        Row(verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 6.dp)) {
                            GeneratedAvatar(seed = n.hashCode(), initials = n.split(" ").map { it.first() }.joinToString(""), size = 40)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(n, style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                                Text(t, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            if (myShifts.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    "Upcoming shifts",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                myShifts.forEachIndexed { index, shift ->
                    var visible by remember(shift.id) { mutableStateOf(false) }
                    LaunchedEffect(shift.id) {
                        delay(index * 80L)
                        visible = true
                    }
                    AnimatedVisibility(
                        visible = visible,
                        enter = slideInVertically(initialOffsetY = { it / 3 }, animationSpec = tween(350)) +
                                fadeIn(animationSpec = tween(350))
                    ) {
                        Column {
                            Spacer(Modifier.height(8.dp))
                            com.carepulse.app.ui.components.PastelCard {
                                Column {
                                    Text(
                                        shift.patientName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        "${shift.dateLabel} · ${shift.timeSlot}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (shift.status == com.carepulse.app.data.model.BookingStatus.CONFIRMED) {
                                        Spacer(Modifier.height(8.dp))
                                        com.carepulse.app.ui.components.PrimaryButton(
                                            text = "Clock in",
                                            onClick = { vm.clockIn(shift.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))
            PrimaryButton(text = "Clock out & write summary", onClick = onClockOut)
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun Stat(modifier: Modifier = Modifier, title: String, value: String, color: Color) {
    Box(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(color)
            .padding(14.dp)
    ) {
        Column {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            Spacer(Modifier.height(2.dp))
            Text(value, style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        }
    }
}
