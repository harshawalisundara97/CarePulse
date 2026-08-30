@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.carepulse.app.ui.screens.caregiver

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import com.carepulse.app.data.model.MedicationItem
import com.carepulse.app.data.model.Mood
import com.carepulse.app.ui.components.CarePulseTextField
import com.carepulse.app.ui.components.PastelCard
import com.carepulse.app.ui.components.PastelChip
import com.carepulse.app.ui.components.PrimaryButton
import com.carepulse.app.viewmodel.CarePulseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShiftSummaryScreen(
    vm: CarePulseViewModel,
    onSubmit: () -> Unit,
    onBack: () -> Unit
) {
    val medsState = remember {
        mutableStateOf(listOf(
            MedicationItem("Lisinopril", "10 mg", true),
            MedicationItem("Metformin", "500 mg", true),
            MedicationItem("Vitamin D", "1000 IU", false)
        ))
    }
    var behavior by remember { mutableStateOf("") }
    var summary by remember { mutableStateOf("") }
    var heartRate by remember { mutableStateOf("72") }
    var systolic by remember { mutableStateOf("122") }
    var diastolic by remember { mutableStateOf("78") }
    var meals by remember { mutableStateOf(3) }
    var mood by remember { mutableStateOf(Mood.HAPPY) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shift Summary Report", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            PastelCard {
                Column {
                    Text("Medications given", style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    medsState.value.forEachIndexed { i, m ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    medsState.value = medsState.value.toMutableList().also {
                                        it[i] = m.copy(administered = !m.administered)
                                    }
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (m.administered) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
                                null,
                                tint = if (m.administered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(m.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                                Text(m.dose, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            PastelCard {
                Column {
                    Text("Vitals", style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CarePulseTextField(
                            value = heartRate,
                            onValueChange = { heartRate = it.filter { c -> c.isDigit() } },
                            label = "Heart rate",
                            modifier = Modifier.weight(1f)
                        )
                        CarePulseTextField(
                            value = systolic,
                            onValueChange = { systolic = it.filter { c -> c.isDigit() } },
                            label = "Systolic",
                            modifier = Modifier.weight(1f)
                        )
                        CarePulseTextField(
                            value = diastolic,
                            onValueChange = { diastolic = it.filter { c -> c.isDigit() } },
                            label = "Diastolic",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("Mood", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    Text("Meals eaten: $meals / 3",
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(0, 1, 2, 3).forEach { v ->
                            PastelChip("$v", selected = v == meals, onClick = { meals = v })
                        }
                    }
                }
            }

            PastelCard {
                Column {
                    Text("Behavior notes", style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    CarePulseTextField(value = behavior, onValueChange = { behavior = it },
                        label = "Mood swings, agitation, etc.")
                }
            }

            PastelCard {
                Column {
                    Text("Day summary", style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    CarePulseTextField(value = summary, onValueChange = { summary = it },
                        label = "How was the day?")
                }
            }

            Spacer(Modifier.height(8.dp))
            PrimaryButton(
                text = "Submit & clock out",
                onClick = {
                    vm.submitShiftReport(
                        medications = medsState.value,
                        behaviorNotes = behavior.ifBlank { "No concerns noted." },
                        daySummary = summary.ifBlank { "Stable, comfortable day." },
                        heartRate = heartRate.toIntOrNull() ?: 72,
                        systolic = systolic.toIntOrNull() ?: 120,
                        diastolic = diastolic.toIntOrNull() ?: 78,
                        mood = mood,
                        mealsEaten = meals
                    )
                    onSubmit()
                }
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
