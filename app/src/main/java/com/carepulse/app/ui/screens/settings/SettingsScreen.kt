@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.carepulse.app.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.carepulse.app.data.model.UserRole
import com.carepulse.app.ui.components.GeneratedAvatar
import com.carepulse.app.ui.components.PastelCard
import com.carepulse.app.ui.theme.CreamBackground
import com.carepulse.app.ui.theme.InkPrimary
import com.carepulse.app.ui.theme.InkSecondary
import com.carepulse.app.ui.theme.PastelMintDeep
import com.carepulse.app.viewmodel.CarePulseViewModel

@Composable
fun SettingsScreen(vm: CarePulseViewModel, onSignOut: () -> Unit) {
    val profile by vm.profile.collectAsState()
    val name = profile?.displayName?.ifBlank { "CarePulse user" } ?: "CarePulse user"
    val email = profile?.email ?: "—"
    val roleLabel = when (profile?.role) {
        UserRole.CAREGIVER -> "Caregiver"
        UserRole.CUSTOMER -> "Family member"
        UserRole.AGENCY -> "Agency admin"
        null -> ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Settings", style = MaterialTheme.typography.headlineMedium,
                        color = InkPrimary, fontWeight = FontWeight.Bold)
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile header
            PastelCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GeneratedAvatar(
                        seed = name.length,
                        initials = name.split(" ").mapNotNull { it.firstOrNull()?.toString() }
                            .take(2).joinToString(""),
                        size = 60
                    )
                    Spacer(Modifier.size(14.dp))
                    Column {
                        Text(name, style = MaterialTheme.typography.titleLarge,
                            color = InkPrimary, fontWeight = FontWeight.Bold)
                        Text(email, style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
                        if (roleLabel.isNotEmpty()) {
                            Text(roleLabel, style = MaterialTheme.typography.labelLarge,
                                color = PastelMintDeep, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Switch between Family / Caregiver / Agency on the same account
            RoleSwitchCard(
                active = profile?.role,
                enrolled = profile?.enrolledRoles ?: emptyList(),
                onSelect = { r -> vm.selectRole(r) },
                onSelectAgency = { companyName -> vm.selectRole(UserRole.AGENCY, companyName) }
            )

            PastelCard {
                Column {
                    SettingRow(Icons.Filled.PersonOutline, "Account") {}
                    SettingRow(Icons.Filled.Notifications, "Notifications") {}
                    SettingRow(Icons.Filled.Shield, "Privacy & security") {}
                    SettingRow(Icons.AutoMirrored.Filled.HelpOutline, "Help & support") {}
                    SettingRow(Icons.Filled.Info, "About CarePulse") {}
                }
            }

            OutlinedButton(
                onClick = onSignOut,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(26.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, null, tint = InkPrimary,
                    modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text("Sign out", color = InkPrimary, fontWeight = FontWeight.SemiBold)
            }

            Text("CarePulse v1.0.0", style = MaterialTheme.typography.bodySmall,
                color = InkSecondary, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
        }
    }
}

@Composable
private fun RoleSwitchCard(
    active: UserRole?,
    enrolled: List<UserRole>,
    onSelect: (UserRole) -> Unit,
    onSelectAgency: (String) -> Unit
) {
    var showAgencyDialog by remember { mutableStateOf(false) }
    var companyName by remember { mutableStateOf("") }

    PastelCard {
        Column {
            Text("Switch mode", style = MaterialTheme.typography.titleMedium,
                color = InkPrimary, fontWeight = FontWeight.SemiBold)
            Text("Use this account as family, caregiver, or agency.",
                style = MaterialTheme.typography.bodySmall, color = InkSecondary)
            Spacer(Modifier.height(12.dp))

            RoleOption(Icons.Filled.Favorite, "Family",
                isActive = active == UserRole.CUSTOMER,
                isEnrolled = enrolled.contains(UserRole.CUSTOMER),
                onClick = { onSelect(UserRole.CUSTOMER) })
            RoleOption(Icons.Filled.MedicalServices, "Caregiver",
                isActive = active == UserRole.CAREGIVER,
                isEnrolled = enrolled.contains(UserRole.CAREGIVER),
                onClick = { onSelect(UserRole.CAREGIVER) })
            RoleOption(Icons.Filled.Business, "Agency",
                isActive = active == UserRole.AGENCY,
                isEnrolled = enrolled.contains(UserRole.AGENCY),
                onClick = {
                    if (enrolled.contains(UserRole.AGENCY)) onSelect(UserRole.AGENCY)
                    else showAgencyDialog = true
                })
        }
    }

    if (showAgencyDialog) {
        AlertDialog(
            onDismissRequest = { showAgencyDialog = false },
            title = { Text("Create your agency") },
            text = {
                Column {
                    Text("Enter your company name to start managing caregivers.",
                        style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = companyName,
                        onValueChange = { companyName = it },
                        label = { Text("Company name") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = companyName.isNotBlank(),
                    onClick = { onSelectAgency(companyName.trim()); showAgencyDialog = false }
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showAgencyDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun RoleOption(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    isEnrolled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isActive) PastelMintDeep.copy(alpha = 0.15f) else Color.Transparent,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clickable(enabled = !isActive) { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = PastelMintDeep, modifier = Modifier.size(22.dp))
            Spacer(Modifier.size(14.dp))
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.bodyLarge, color = InkPrimary)
                Text(
                    when {
                        isActive -> "Current mode"
                        isEnrolled -> "Tap to switch"
                        else -> "Tap to add this role"
                    },
                    style = MaterialTheme.typography.bodySmall, color = InkSecondary
                )
            }
            if (isActive) {
                Icon(Icons.Filled.CheckCircle, null, tint = PastelMintDeep,
                    modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun SettingRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = PastelMintDeep, modifier = Modifier.size(22.dp))
        Spacer(Modifier.size(14.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge,
            color = InkPrimary, modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null,
            tint = InkSecondary, modifier = Modifier.size(20.dp))
    }
}
