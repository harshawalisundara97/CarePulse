@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.carepulse.app.ui.screens.caregiver

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.carepulse.app.ui.components.CarePulseTextField
import com.carepulse.app.ui.components.PrimaryButton
import com.carepulse.app.ui.theme.CreamBackground
import com.carepulse.app.ui.theme.InkPrimary
import com.carepulse.app.viewmodel.CarePulseViewModel

@Composable
fun EditProfileScreen(vm: CarePulseViewModel, onDone: () -> Unit) {
    val caregivers by vm.caregivers.collectAsState()
    val profile by vm.profile.collectAsState()
    val myCg = caregivers.firstOrNull { it.id == profile?.uid }

    var bio by remember(myCg) { mutableStateOf(myCg?.bio ?: "") }
    var specializations by remember(myCg) {
        mutableStateOf(myCg?.specializations?.joinToString(", ") ?: "")
    }
    var hourlyRate by remember(myCg) { mutableStateOf(myCg?.hourlyRate?.toString() ?: "") }
    var availability by remember(myCg) {
        mutableStateOf(myCg?.availability?.joinToString(", ") ?: "")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Edit Profile", color = InkPrimary, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = InkPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = CreamBackground
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CarePulseTextField(
                value = bio,
                onValueChange = { bio = it },
                label = "Bio",
                singleLine = false
            )
            CarePulseTextField(
                value = specializations,
                onValueChange = { specializations = it },
                label = "Skills (comma-separated, e.g. Elderly Care, Post-Op)"
            )
            CarePulseTextField(
                value = hourlyRate,
                onValueChange = { hourlyRate = it.filter { c -> c.isDigit() } },
                label = "Hourly rate (LKR)"
            )
            CarePulseTextField(
                value = availability,
                onValueChange = { availability = it },
                label = "Availability (e.g. Mon AM, Tue PM)"
            )
            Spacer(Modifier.height(8.dp))
            PrimaryButton(
                text = "Save changes",
                onClick = {
                    vm.updateCaregiverProfile(
                        bio = bio,
                        specializations = specializations
                            .split(",")
                            .map { it.trim() }
                            .filter { it.isNotBlank() },
                        hourlyRate = hourlyRate.toIntOrNull() ?: 0,
                        availability = availability
                            .split(",")
                            .map { it.trim() }
                            .filter { it.isNotBlank() }
                    )
                    onDone()
                }
            )
        }
    }
}
