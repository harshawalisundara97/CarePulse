@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.animation.ExperimentalAnimationApi::class)

package com.carepulse.app.ui.screens.customer

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.carepulse.app.R
import com.carepulse.app.ui.components.CarePulseTextField
import com.carepulse.app.ui.components.PastelChip
import com.carepulse.app.ui.components.PrimaryButton
import com.carepulse.app.ui.theme.GlassScreen
import com.carepulse.app.ui.theme.Motion
import com.carepulse.app.ui.theme.Radii
import com.carepulse.app.ui.theme.Spacing
import com.carepulse.app.ui.theme.StatusAvailable
import com.carepulse.app.ui.theme.TypeNumericM
import com.carepulse.app.ui.theme.glassCard
import com.carepulse.app.viewmodel.CarePulseViewModel
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    caregiverId: String,
    vm: CarePulseViewModel,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    val caregiver = vm.caregiverById(caregiverId) ?: return
    var step by remember { mutableStateOf(0) }

    val dates = listOf("Mon Jun 3", "Tue Jun 4", "Wed Jun 5", "Thu Jun 6", "Fri Jun 7")
    val times = listOf("9:00 AM - 1:00 PM", "1:00 PM - 5:00 PM", "5:00 PM - 9:00 PM")

    var date by remember { mutableStateOf(dates.first()) }
    var time by remember { mutableStateOf(times.first()) }
    var patientName by remember { mutableStateOf("") }

    GlassScreen { hazeState ->
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text("Book ${caregiver.name}", color = MaterialTheme.colorScheme.onSurface) },
                    navigationIcon = {
                        IconButton(onClick = { if (step == 0) onBack() else step-- }) {
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
            containerColor = Color.Transparent
        ) { padding ->
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(Spacing.ScreenPaddingCompact)
            ) {
                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        (slideInHorizontally { it } + fadeIn()) togetherWith
                            (slideOutHorizontally { -it } + fadeOut()) using SizeTransform(clip = false)
                    },
                    label = "bookingStep"
                ) { current ->
                    when (current) {
                        0 -> StepPick(
                            dates, times, date, time,
                            onDate = { date = it }, onTime = { time = it },
                            onNext = { step = 1 },
                            hazeState = hazeState
                        )
                        1 -> StepConfirm(
                            caregiverName = caregiver.name,
                            hourlyRate = caregiver.hourlyRate,
                            date = date, time = time,
                            patientName = patientName,
                            onPatientName = { patientName = it },
                            onConfirm = {
                                vm.confirmBooking(caregiver, patientName.ifBlank { "My loved one" }, date, time, 4)
                                step = 2
                            },
                            hazeState = hazeState
                        )
                        2 -> StepSuccess(onDone = onComplete)
                    }
                }
            }
        }
    }
}

@Composable
private fun StepPick(
    dates: List<String>, times: List<String>,
    date: String, time: String,
    onDate: (String) -> Unit, onTime: (String) -> Unit,
    onNext: () -> Unit,
    hazeState: HazeState
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("1 of 3 — Select date & time",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        Text("When do you need care?",
            style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)

        Box(
            Modifier
                .fillMaxWidth()
                .glassCard(radius = Radii.CardLarge, hazeState = hazeState)
                .padding(Spacing.CardPaddingCompact)
        ) {
            Column {
                Text("Date", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    dates.forEach { d ->
                        PastelChip(d, selected = d == date, onClick = { onDate(d) })
                    }
                }
            }
        }
        Box(
            Modifier
                .fillMaxWidth()
                .glassCard(radius = Radii.CardLarge, hazeState = hazeState)
                .padding(Spacing.CardPaddingCompact)
        ) {
            Column {
                Text("Time slot", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    times.forEach { t ->
                        PastelChip(t, selected = t == time, onClick = { onTime(t) })
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        PrimaryButton(text = "Continue", onClick = onNext)
    }
}

/** Length in hours of every bookable time slot. */
internal const val BookingSlotHours = 4

/** Booking total for [hours] at [hourlyRate], formatted for display, e.g. "LKR 22,200". */
internal fun formatBookingTotal(hourlyRate: Int, hours: Int = BookingSlotHours): String =
    "LKR " + NumberFormat.getNumberInstance(Locale.US).format(hourlyRate * hours)

@Composable
private fun StepConfirm(
    caregiverName: String, hourlyRate: Int,
    date: String, time: String, patientName: String,
    onPatientName: (String) -> Unit, onConfirm: () -> Unit,
    hazeState: HazeState
) {
    // Booking total: rate x hours, where hours is the fixed 4-hour length of every time slot
    // offered in `times` above. This mirrors CarePulseViewModel.confirmBooking's own
    // `caregiver.hourlyRate * hours` computation (hours = 4 there too) -- see task-11-report.md
    // for the pre-existing "hours" hardcode this screen and the ViewModel share, which is out of
    // this styling task's scope. Only the display formatting (LKR + thousands separator) below
    // is new; the multiplication itself is unchanged.
    val estimatedHours = BookingSlotHours
    val formattedTotal = formatBookingTotal(hourlyRate, estimatedHours)

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("2 of 3 — Confirm details",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        Text("Almost done", style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)

        Box(
            Modifier
                .fillMaxWidth()
                .glassCard(radius = Radii.CardLarge, hazeState = hazeState)
                .padding(Spacing.CardPaddingCompact)
        ) {
            Column {
                Row {
                    Text("Caregiver", Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
                    Text(caregiverName, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(6.dp))
                Row {
                    Text("Date", Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
                    Text(date, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(6.dp))
                Row {
                    Text("Time", Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
                    Text(time, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                }
                androidx.compose.material3.HorizontalDivider(Modifier.padding(vertical = 10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Estimated total ($estimatedHours hrs)", Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
                    Text(formattedTotal, color = MaterialTheme.colorScheme.onSurface, style = TypeNumericM)
                }
            }
        }

        CarePulseTextField(value = patientName, onValueChange = onPatientName, label = "Patient's name")

        Spacer(Modifier.height(8.dp))
        PrimaryButton(text = "Pay & confirm", onClick = onConfirm)
    }
}

@Composable
private fun StepSuccess(onDone: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(150); visible = true }
    val tickScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = Motion.BottomSheet, easing = Motion.Emphasized),
        label = "successTickScale"
    )
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(120.dp)
                .scale(tickScale)
                .clip(CircleShape)
                .background(StatusAvailable.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = StatusAvailable, modifier = Modifier.size(72.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text("Booking confirmed!", style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("We've notified your caregiver. You can track everything from the Pulse Dashboard.",
            style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 24.dp))
        Spacer(Modifier.height(36.dp))
        PrimaryButton(text = "Back to home", onClick = onDone)
    }
}
