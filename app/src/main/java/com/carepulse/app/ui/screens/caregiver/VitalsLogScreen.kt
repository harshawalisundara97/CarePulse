@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.carepulse.app.ui.screens.caregiver

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.carepulse.app.data.model.Mood
import com.carepulse.app.ui.components.CarePulseTextField
import com.carepulse.app.ui.components.PastelCard
import com.carepulse.app.ui.components.PastelChip
import com.carepulse.app.ui.components.PrimaryButton
import com.carepulse.app.ui.theme.Background
import com.carepulse.app.ui.theme.TextPrimary
import com.carepulse.app.ui.theme.TextSecondary
import com.carepulse.app.viewmodel.CarePulseViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VitalsLogScreen(
    vm: CarePulseViewModel,
    onBack: () -> Unit
) {
    var heartRate by remember { mutableStateOf("72") }
    var systolic by remember { mutableStateOf("120") }
    var diastolic by remember { mutableStateOf("80") }
    var mood by remember { mutableStateOf(Mood.CALM) }
    var meals by remember { mutableIntStateOf(3) }
    var notes by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Log Vitals", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "Record a quick vitals snapshot — it will appear on the family's Pulse dashboard immediately.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            PastelCard {
                Column {
                    Text(
                        "Vitals",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(10.dp))
                    CarePulseTextField(
                        value = heartRate,
                        onValueChange = { heartRate = it.filter(Char::isDigit) },
                        label = "Heart rate (bpm)"
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CarePulseTextField(
                            value = systolic,
                            onValueChange = { systolic = it.filter(Char::isDigit) },
                            label = "Systolic",
                            modifier = Modifier.weight(1f)
                        )
                        CarePulseTextField(
                            value = diastolic,
                            onValueChange = { diastolic = it.filter(Char::isDigit) },
                            label = "Diastolic",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            PastelCard {
                Column {
                    Text(
                        "Mood",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Mood.values().forEach { m ->
                            PastelChip(
                                label = m.label,
                                selected = m == mood,
                                onClick = { mood = m },
                                leadingIcon = m.icon
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Meals eaten: $meals / 3",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(0, 1, 2, 3).forEach { v ->
                            PastelChip("$v", selected = v == meals, onClick = { meals = v })
                        }
                    }
                }
            }

            PastelCard {
                Column {
                    Text(
                        "Notes",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    CarePulseTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = "Any observations…",
                        singleLine = false
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            PrimaryButton(
                text = "Save vitals",
                onClick = {
                    vm.logVitals(
                        heartRate = heartRate.toIntOrNull() ?: 72,
                        systolic = systolic.toIntOrNull() ?: 120,
                        diastolic = diastolic.toIntOrNull() ?: 80,
                        mood = mood,
                        mealsEaten = meals,
                        notes = notes.ifBlank { "No additional notes." }
                    )
                    scope.launch {
                        snackbarHostState.showSnackbar("✓ Vitals saved — family dashboard updated")
                    }
                    onBack()
                }
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
