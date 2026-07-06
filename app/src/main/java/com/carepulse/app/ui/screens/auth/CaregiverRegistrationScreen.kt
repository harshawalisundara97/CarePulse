@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.carepulse.app.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.carepulse.app.ui.components.CarePulseTextField
import com.carepulse.app.ui.components.GeneratedAvatar
import com.carepulse.app.ui.components.PastelChip
import com.carepulse.app.ui.components.PrimaryButton
import com.carepulse.app.ui.theme.Background
import com.carepulse.app.ui.theme.TextPrimary
import com.carepulse.app.ui.theme.TextSecondary
import com.carepulse.app.viewmodel.CarePulseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverRegistrationScreen(vm: CarePulseViewModel, onDone: () -> Unit) {
    val displayName by vm.displayName.collectAsState()

    var name by remember { mutableStateOf(displayName) }
    var area by remember { mutableStateOf("") }
    var qualifications by remember { mutableStateOf("") }
    var hourlyRate by remember { mutableStateOf("25") }

    val specOptions = listOf("Elderly Care", "Post-Op Recovery", "Dementia Care", "Companion Care", "Wound Care")
    var selectedSpec by remember { mutableStateOf(specOptions.first()) }

    val availabilityOptions = listOf("Mon AM", "Mon PM", "Tue AM", "Tue PM", "Wed AM", "Wed PM", "Thu AM", "Thu PM", "Fri AM", "Fri PM", "Sat", "Sun")
    val selectedAvailability = remember { mutableStateOf(setOf("Mon AM", "Wed PM")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Caregiver profile", color = TextPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Background
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                GeneratedAvatar(seed = name.hashCode(), initials = name.ifBlank { "NC" }.split(" ").map { it.first() }.joinToString(""), size = 96)
            }
            Text(
                "Tap to set a profile photo",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.fillMaxWidth(),
            )

            CarePulseTextField(value = name, onValueChange = { name = it }, label = "Full name",
                capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Words)
            CarePulseTextField(value = area, onValueChange = { area = it }, label = "Service area / Zip", placeholder = "Downtown · 90210")
            CarePulseTextField(value = qualifications, onValueChange = { qualifications = it }, label = "Qualifications", placeholder = "Registered Nurse, 5 yrs experience")
            CarePulseTextField(value = hourlyRate, onValueChange = { hourlyRate = it.filter { c -> c.isDigit() } }, label = "Hourly rate (USD)")

            Text("Specialization", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Row(
                Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {}
            FlowRowChips(specOptions, selectedSpec) { selectedSpec = it }

            Text("Availability", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            FlowRowMulti(availabilityOptions, selectedAvailability.value) { slot ->
                val s = selectedAvailability.value.toMutableSet()
                if (s.contains(slot)) s.remove(slot) else s.add(slot)
                selectedAvailability.value = s
            }

            Spacer(Modifier.height(8.dp))
            PrimaryButton(
                text = "Complete registration",
                onClick = {
                    vm.registerCaregiver(
                        name = name,
                        area = area,
                        qualifications = qualifications,
                        specialization = selectedSpec,
                        hourlyRate = hourlyRate.toIntOrNull() ?: 25
                    )
                    onDone()
                },
                enabled = name.isNotBlank() && area.isNotBlank()
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FlowRowChips(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        options.forEach { PastelChip(it, selected = it == selected, onClick = { onSelect(it) }) }
    }
}

@Composable
private fun FlowRowMulti(options: List<String>, selected: Set<String>, onToggle: (String) -> Unit) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        options.forEach { PastelChip(it, selected = selected.contains(it), onClick = { onToggle(it) }) }
    }
}
