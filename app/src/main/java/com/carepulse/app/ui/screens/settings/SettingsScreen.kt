@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.carepulse.app.ui.screens.settings

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.carepulse.app.data.model.UserRole
import com.carepulse.app.data.photo.ProfilePhotoManager
import com.carepulse.app.ui.components.PastelCard
import com.carepulse.app.ui.components.ProfileAvatar
import com.carepulse.app.ui.theme.Background
import com.carepulse.app.ui.theme.NavyPrimary
import com.carepulse.app.ui.theme.TealAccent
import com.carepulse.app.ui.theme.TextPrimary
import com.carepulse.app.ui.theme.TextSecondary
import com.carepulse.app.viewmodel.CarePulseViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(vm: CarePulseViewModel, onSignOut: () -> Unit) {
    val profile by vm.profile.collectAsState()
    val photoPath by vm.profilePhotoPath.collectAsState()
    val context = LocalContext.current
    val name = profile?.displayName?.ifBlank { "CarePulse user" } ?: "CarePulse user"
    val email = profile?.email ?: "—"
    val roleLabel = when (profile?.role) {
        UserRole.CAREGIVER -> "Caregiver"
        UserRole.CUSTOMER  -> "Family member"
        UserRole.AGENCY    -> "Agency admin"
        null               -> ""
    }
    val initials = name.split(" ").mapNotNull { it.firstOrNull()?.toString() }
        .take(2).joinToString("")

    var showPhotoPicker by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    // Camera output URI — set before launching camera
    var cameraOutputUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) cameraOutputUri?.let { vm.saveProfilePhoto(context, it) }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { vm.saveProfilePhoto(context, it) }
    }

    val cameraPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = ProfilePhotoManager.cameraOutputUri(context)
            cameraOutputUri = uri
            cameraLauncher.launch(uri)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Background
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile header with tappable avatar
            PastelCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.clickable { showPhotoPicker = true }) {
                        ProfileAvatar(
                            photoPath = photoPath,
                            initials = initials,
                            seed = name.length,
                            size = 60
                        )
                        // Camera badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(20.dp)
                                .background(TealAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.AddAPhoto,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                    Spacer(Modifier.size(14.dp))
                    Column {
                        Text(
                            name,
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(email, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        if (roleLabel.isNotEmpty()) {
                            Text(
                                roleLabel,
                                style = MaterialTheme.typography.labelLarge,
                                color = TealAccent,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.size(8.dp))
                Text("Sign out", color = TextPrimary, fontWeight = FontWeight.SemiBold)
            }

            Text(
                "CarePulse v1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
        }
    }

    // Photo picker bottom sheet
    if (showPhotoPicker) {
        ModalBottomSheet(
            onDismissRequest = { showPhotoPicker = false },
            sheetState = sheetState,
            containerColor = NavyPrimary
        ) {
            Column(
                Modifier
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    "Profile photo",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                PhotoSheetOption(icon = Icons.Filled.CameraAlt, label = "Take Photo") {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showPhotoPicker = false
                    }
                    val hasCamPerm = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasCamPerm) {
                        val uri = ProfilePhotoManager.cameraOutputUri(context)
                        cameraOutputUri = uri
                        cameraLauncher.launch(uri)
                    } else {
                        cameraPermLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
                PhotoSheetOption(icon = Icons.Filled.Image, label = "Choose from Gallery") {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showPhotoPicker = false
                    }
                    galleryLauncher.launch("image/*")
                }
            }
        }
    }
}

@Composable
private fun PhotoSheetOption(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(icon, contentDescription = null, tint = TealAccent, modifier = Modifier.size(24.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
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
            Text(
                "Switch mode",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "Use this account as family, caregiver, or agency.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(Modifier.height(12.dp))
            RoleOption(
                Icons.Filled.Favorite, "Family",
                isActive = active == UserRole.CUSTOMER,
                isEnrolled = enrolled.contains(UserRole.CUSTOMER),
                onClick = { onSelect(UserRole.CUSTOMER) }
            )
            RoleOption(
                Icons.Filled.MedicalServices, "Caregiver",
                isActive = active == UserRole.CAREGIVER,
                isEnrolled = enrolled.contains(UserRole.CAREGIVER),
                onClick = { onSelect(UserRole.CAREGIVER) }
            )
            RoleOption(
                Icons.Filled.Business, "Agency",
                isActive = active == UserRole.AGENCY,
                isEnrolled = enrolled.contains(UserRole.AGENCY),
                onClick = {
                    if (enrolled.contains(UserRole.AGENCY)) onSelect(UserRole.AGENCY)
                    else showAgencyDialog = true
                }
            )
        }
    }

    if (showAgencyDialog) {
        AlertDialog(
            onDismissRequest = { showAgencyDialog = false },
            title = { Text("Create your agency") },
            text = {
                Column {
                    Text(
                        "Enter your company name to start managing caregivers.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
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
        color = if (isActive) TealAccent.copy(alpha = 0.15f) else Color.Transparent,
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
            Icon(icon, contentDescription = null, tint = TealAccent, modifier = Modifier.size(22.dp))
            Spacer(Modifier.size(14.dp))
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                Text(
                    when {
                        isActive   -> "Current mode"
                        isEnrolled -> "Tap to switch"
                        else       -> "Tap to add this role"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            if (isActive) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = TealAccent,
                    modifier = Modifier.size(20.dp)
                )
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
        Icon(icon, contentDescription = null, tint = TealAccent, modifier = Modifier.size(22.dp))
        Spacer(Modifier.size(14.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}
