@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class, androidx.compose.animation.ExperimentalAnimationApi::class)

package com.carepulse.app.ui.screens.customer

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.carepulse.app.ui.components.CarePulseTextField
import com.carepulse.app.ui.components.PastelCard
import com.carepulse.app.ui.components.PastelChip
import com.carepulse.app.ui.components.PrimaryButton
import com.carepulse.app.ui.theme.Background
import com.carepulse.app.ui.theme.TealLight
import com.carepulse.app.ui.theme.TextPrimary
import com.carepulse.app.ui.theme.TextSecondary
import com.carepulse.app.viewmodel.CarePulseViewModel
import kotlinx.coroutines.delay

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book ${caregiver.name}", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { if (step == 0) onBack() else step-- }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Background
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).padding(20.dp)) {
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
                        onNext = { step = 1 }
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
                        }
                    )
                    2 -> StepSuccess(onDone = onComplete)
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
    onNext: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("1 of 3 — Select date & time",
            style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Text("When do you need care?",
            style = MaterialTheme.typography.headlineMedium, color = TextPrimary, fontWeight = FontWeight.Bold)

        PastelCard {
            Column {
                Text("Date", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    dates.forEach { d ->
                        PastelChip(d, selected = d == date, onClick = { onDate(d) })
                    }
                }
            }
        }
        PastelCard {
            Column {
                Text("Time slot", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
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

@Composable
private fun StepConfirm(
    caregiverName: String, hourlyRate: Int,
    date: String, time: String, patientName: String,
    onPatientName: (String) -> Unit, onConfirm: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("2 of 3 — Confirm details",
            style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Text("Almost done", style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary, fontWeight = FontWeight.Bold)

        PastelCard {
            Column {
                Row {
                    Text("Caregiver", Modifier.weight(1f), color = TextSecondary)
                    Text(caregiverName, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(6.dp))
                Row {
                    Text("Date", Modifier.weight(1f), color = TextSecondary)
                    Text(date, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(6.dp))
                Row {
                    Text("Time", Modifier.weight(1f), color = TextSecondary)
                    Text(time, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                }
                androidx.compose.material3.HorizontalDivider(Modifier.padding(vertical = 10.dp))
                Row {
                    Text("Estimated total (4 hrs)", Modifier.weight(1f), color = TextSecondary)
                    Text("\$${hourlyRate * 4}", color = TextPrimary, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium)
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
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(TealLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.CheckCircle, null, tint = Color.White, modifier = Modifier.size(72.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text("Booking confirmed!", style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("We've notified your caregiver. You can track everything from the Pulse Dashboard.",
            style = MaterialTheme.typography.bodyLarge, color = TextSecondary,
            modifier = Modifier.padding(horizontal = 24.dp))
        Spacer(Modifier.height(36.dp))
        PrimaryButton(text = "Back to home", onClick = onDone)
    }
}
