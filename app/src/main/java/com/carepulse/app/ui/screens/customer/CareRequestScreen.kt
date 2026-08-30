@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.carepulse.app.ui.screens.customer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.carepulse.app.data.model.CareType
import com.carepulse.app.data.model.Gender
import com.carepulse.app.ui.components.CarePulseTextField
import com.carepulse.app.ui.components.PastelCard
import com.carepulse.app.ui.components.PastelChip
import com.carepulse.app.ui.components.PrimaryButton
import com.carepulse.app.viewmodel.CarePulseViewModel

@Composable
fun CareRequestScreen(
    vm: CarePulseViewModel,
    onSubmitted: () -> Unit,
    onBack: () -> Unit
) {
    val agencies by vm.agencies.collectAsState()
    LaunchedEffect(Unit) { vm.loadAgencies() }

    var agencyId by remember { mutableStateOf<String?>(null) }
    var patientName by remember { mutableStateOf("") }
    var patientGender by remember { mutableStateOf(Gender.FEMALE) }
    var preferredGender by remember { mutableStateOf(Gender.FEMALE) }
    var careType by remember { mutableStateOf(CareType.HOME) }
    var notes by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Request a caregiver", color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold) },
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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Choose agency
            Label("Choose a caregiving company")
            if (agencies.isEmpty()) {
                Text("No companies available yet.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    agencies.forEach { a ->
                        PastelChip(
                            label = a.name + if (a.nearHospital.isNotBlank())
                                " · ${a.nearHospital}" else "",
                            selected = a.id == agencyId,
                            onClick = { agencyId = a.id }
                        )
                    }
                }
            }

            CarePulseTextField(value = patientName, onValueChange = { patientName = it },
                label = "Patient name",
                capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Words)

            Label("Patient gender")
            GenderRow(patientGender) { patientGender = it }

            Label("Preferred caregiver gender")
            GenderRow(preferredGender) { preferredGender = it }

            Label("Care location")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CareType.values().forEach { t ->
                    PastelChip(
                        label = t.name.lowercase().replaceFirstChar { it.uppercase() },
                        selected = t == careType,
                        onClick = { careType = t }
                    )
                }
            }

            CarePulseTextField(value = notes, onValueChange = { notes = it },
                label = "Notes (needs, schedule…)", singleLine = false)

            Spacer(Modifier.height(4.dp))
            PrimaryButton(
                text = "Submit request",
                onClick = {
                    agencyId?.let {
                        vm.submitCareRequest(it, patientName, patientGender,
                            preferredGender, careType, notes)
                        onSubmitted()
                    }
                },
                enabled = agencyId != null && patientName.isNotBlank()
            )
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun Label(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun GenderRow(selected: Gender, onSelect: (Gender) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Gender.values().forEach { g ->
            PastelChip(
                label = g.name.lowercase().replaceFirstChar { it.uppercase() },
                selected = g == selected,
                onClick = { onSelect(g) }
            )
        }
    }
}
