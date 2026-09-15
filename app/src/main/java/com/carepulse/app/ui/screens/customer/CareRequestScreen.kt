@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.carepulse.app.ui.screens.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.carepulse.app.R
import com.carepulse.app.data.model.CareType
import com.carepulse.app.data.model.Gender
import com.carepulse.app.ui.components.CarePulseTextField
import com.carepulse.app.ui.components.PastelChip
import com.carepulse.app.ui.components.PrimaryButton
import com.carepulse.app.ui.theme.GlassFillSubtle
import com.carepulse.app.ui.theme.GlassFillSubtleDark
import com.carepulse.app.ui.theme.GlassScreen
import com.carepulse.app.ui.theme.LocalIsDarkTheme
import com.carepulse.app.ui.theme.Radii
import com.carepulse.app.ui.theme.Spacing
import com.carepulse.app.ui.theme.glassCard
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

    GlassScreen { hazeState ->
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text("Request a caregiver", color = MaterialTheme.colorScheme.onSurface) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
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
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = Spacing.ScreenPaddingCompact)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.CardGap)
            ) {
                Spacer(Modifier.height(0.dp))

                // Choose agency
                Box(
                    Modifier
                        .fillMaxWidth()
                        .glassCard(radius = Radii.CardLarge, hazeState = hazeState)
                        .padding(Spacing.CardPaddingCompact)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Label("Choose a caregiving company")
                        if (agencies.isEmpty()) {
                            Text(
                                "No companies available yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
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
                    }
                }

                // Request details
                Box(
                    Modifier
                        .fillMaxWidth()
                        .glassCard(radius = Radii.CardLarge, hazeState = hazeState)
                        .padding(Spacing.CardPaddingCompact)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        CarePulseTextField(
                            value = patientName, onValueChange = { patientName = it },
                            label = "Patient name",
                            capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Words
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Label("Patient gender")
                            GenderSegmentedRow(patientGender) { patientGender = it }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Label("Preferred caregiver gender")
                            GenderSegmentedRow(preferredGender) { preferredGender = it }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Label("Care location")
                            CareTypeSegmentedRow(careType) { careType = it }
                        }

                        CarePulseTextField(
                            value = notes, onValueChange = { notes = it },
                            label = "Notes (needs, schedule…)", singleLine = false
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))
                PrimaryButton(
                    text = "Submit request",
                    onClick = {
                        agencyId?.let {
                            vm.submitCareRequest(
                                it, patientName, patientGender,
                                preferredGender, careType, notes
                            )
                            onSubmitted()
                        }
                    },
                    enabled = agencyId != null && patientName.isNotBlank()
                )
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun Label(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
}

/**
 * Segmented selector per the glass-on-grid design notes: equal-width Radii.Chip-shaped
 * segments in a single track, with an AccentPrimary fill on the selected segment. Selection is
 * exposed to accessibility services via Modifier.selectableGroup on the track and
 * Modifier.selectable with `role = Role.RadioButton` on each segment, rather than by color
 * alone.
 */
@Composable
private fun <T> SegmentedRow(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: (T) -> String
) {
    val dark = LocalIsDarkTheme.current
    val trackColor = if (dark) GlassFillSubtleDark else GlassFillSubtle
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radii.Chip))
            .background(trackColor)
            .padding(4.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            Box(
                Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(Radii.Chip))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .selectable(
                        selected = isSelected,
                        onClick = { onSelect(option) },
                        role = Role.RadioButton
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label(option),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun GenderSegmentedRow(selected: Gender, onSelect: (Gender) -> Unit) {
    SegmentedRow(
        options = Gender.values().toList(),
        selected = selected,
        onSelect = onSelect,
        label = { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } }
    )
}

@Composable
private fun CareTypeSegmentedRow(selected: CareType, onSelect: (CareType) -> Unit) {
    SegmentedRow(
        options = CareType.values().toList(),
        selected = selected,
        onSelect = onSelect,
        label = { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } }
    )
}
