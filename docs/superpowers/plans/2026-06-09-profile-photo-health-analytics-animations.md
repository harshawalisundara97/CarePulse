# Profile Photo · Health Analytics · Animations — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add profile photo capture/upload, caregiver any-time vitals logging with family analytics charts, and app-wide polish animations.

**Architecture:** Profile photos are stored locally in `filesDir` (no Firebase Storage needed); path persisted in `SharedPreferences`. Vitals logging is a new standalone screen that writes to the existing Firestore `vitals/` collection. Animations use Compose `animation` APIs already on the classpath — no new dependencies except Coil for image rendering.

**Tech Stack:** Jetpack Compose, Coil 2.7, ActivityResultContracts (camera + gallery), SharedPreferences, Firestore (existing), Compose animation APIs.

---

## Task 1: Add Coil + request camera/gallery permissions

**Files:**
- Modify: `app/build.gradle.kts`
- Modify: `app/src/main/AndroidManifest.xml`

- [ ] **Step 1: Add Coil dependency**

In `app/build.gradle.kts`, inside `dependencies {}`, add after the last `implementation(...)` line:

```kotlin
implementation("io.coil-kt:coil-compose:2.7.0")
```

- [ ] **Step 2: Add camera + storage permissions to AndroidManifest**

In `app/src/main/AndroidManifest.xml`, add before the `<application` tag:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES"
    android:minSdkVersion="33" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />
```

Also add the `FileProvider` inside `<application>`:

```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

- [ ] **Step 3: Create `res/xml/file_paths.xml`**

Create file `app/src/main/res/xml/file_paths.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <files-path name="profile_photos" path="." />
    <cache-path name="camera_temp" path="." />
</paths>
```

- [ ] **Step 4: Sync gradle**

```bash
cd /Users/ranjana/Harsha/CarePulse
./gradlew :app:dependencies --configuration releaseRuntimeClasspath 2>&1 | grep -i coil | head -5
```
Expected: `io.coil-kt:coil-compose:2.7.0` appears in output.

- [ ] **Step 5: Commit**

```bash
cd /Users/ranjana/Harsha/CarePulse
git add app/build.gradle.kts app/src/main/AndroidManifest.xml app/src/main/res/xml/file_paths.xml
git commit -m "feat: add Coil dependency + camera/storage permissions"
```

---

## Task 2: Profile photo storage helper

**Files:**
- Create: `app/src/main/java/com/carepulse/app/data/photo/ProfilePhotoManager.kt`

- [ ] **Step 1: Create `ProfilePhotoManager`**

Create `app/src/main/java/com/carepulse/app/data/photo/ProfilePhotoManager.kt`:

```kotlin
package com.carepulse.app.data.photo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * Manages the single profile photo stored in the app's internal files dir.
 * No cloud storage needed — the photo lives only on this device.
 */
object ProfilePhotoManager {

    private const val PREFS = "carepulse_prefs"
    private const val KEY_PHOTO_PATH = "profile_photo_path"
    private const val PHOTO_FILENAME = "profile_photo.jpg"
    private const val CAMERA_TEMP = "camera_temp.jpg"

    /** Returns the saved photo path, or null if none set. */
    fun getSavedPhotoPath(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_PHOTO_PATH, null)

    /** Returns the file Uri this app uses for camera capture output. */
    fun cameraOutputUri(context: Context): Uri {
        val tempFile = File(context.cacheDir, CAMERA_TEMP)
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile)
    }

    /**
     * Copies a photo (from gallery URI or camera temp file) into internal
     * storage and saves the absolute path to SharedPreferences.
     * Returns the new file path on success, null on failure.
     */
    fun savePhoto(context: Context, sourceUri: Uri): String? = runCatching {
        val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return null
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        val dest = File(context.filesDir, PHOTO_FILENAME)
        dest.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }

        val path = dest.absolutePath
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_PHOTO_PATH, path).apply()
        path
    }.getOrNull()

    /** Clears the saved photo (reverts to generated avatar). */
    fun clearPhoto(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().remove(KEY_PHOTO_PATH).apply()
        File(context.filesDir, PHOTO_FILENAME).delete()
    }
}
```

- [ ] **Step 2: Commit**

```bash
cd /Users/ranjana/Harsha/CarePulse
git add app/src/main/java/com/carepulse/app/data/photo/ProfilePhotoManager.kt
git commit -m "feat: add ProfilePhotoManager for local photo storage"
```

---

## Task 3: ViewModel — profile photo state

**Files:**
- Modify: `app/src/main/java/com/carepulse/app/viewmodel/CarePulseViewModel.kt`

- [ ] **Step 1: Add photo StateFlow + update method**

In `CarePulseViewModel`, after the `displayName` StateFlow (around line 71), add:

```kotlin
private val _profilePhotoPath = MutableStateFlow<String?>(null)
val profilePhotoPath: StateFlow<String?> = _profilePhotoPath.asStateFlow()
```

Then add these two methods at the end of the ViewModel class (before the closing `}`):

```kotlin
fun loadProfilePhoto(context: android.content.Context) {
    _profilePhotoPath.value =
        com.carepulse.app.data.photo.ProfilePhotoManager.getSavedPhotoPath(context)
}

fun saveProfilePhoto(context: android.content.Context, uri: android.net.Uri) {
    val path = com.carepulse.app.data.photo.ProfilePhotoManager.savePhoto(context, uri)
    _profilePhotoPath.value = path
}
```

- [ ] **Step 2: Load photo on init in MainActivity**

In `MainActivity.kt`, inside `onCreate` after `setContent { ... }` has finished setting up, call:

```kotlin
// Load any previously saved profile photo
val vm: CarePulseViewModel by viewModels { CarePulseViewModel.Factory }
vm.loadProfilePhoto(this)
```

Add these imports to `MainActivity.kt`:
```kotlin
import androidx.activity.viewModels
import com.carepulse.app.viewmodel.CarePulseViewModel
```

The full `onCreate` body becomes:
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    val vm: CarePulseViewModel by viewModels { CarePulseViewModel.Factory }
    vm.loadProfilePhoto(this)
    setContent {
        CarePulseTheme {
            Surface(
                modifier = Modifier.fillMaxSize().background(Background),
                color = Background
            ) {
                CarePulseNavGraph()
            }
        }
    }
}
```

- [ ] **Step 3: Commit**

```bash
cd /Users/ranjana/Harsha/CarePulse
git add app/src/main/java/com/carepulse/app/viewmodel/CarePulseViewModel.kt \
        app/src/main/java/com/carepulse/app/MainActivity.kt
git commit -m "feat: add profilePhotoPath state to ViewModel"
```

---

## Task 4: Profile photo UI — avatar + bottom sheet in SettingsScreen

**Files:**
- Modify: `app/src/main/java/com/carepulse/app/ui/screens/settings/SettingsScreen.kt`
- Modify: `app/src/main/java/com/carepulse/app/ui/components/CommonComponents.kt`

- [ ] **Step 1: Create `ProfileAvatar` composable in CommonComponents**

In `CommonComponents.kt`, add these imports at the top with the others:

```kotlin
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import java.io.File
```

Then add this new composable after `GeneratedAvatar`:

```kotlin
/**
 * Shows a real photo if [photoPath] points to an existing file,
 * otherwise falls back to [GeneratedAvatar] with initials.
 */
@Composable
fun ProfileAvatar(
    photoPath: String?,
    initials: String,
    seed: Int,
    size: Int = 56,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val file = photoPath?.let { File(it) }
    val hasPhoto = file?.exists() == true

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .border(2.dp, TealAccent, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (hasPhoto && file != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(file)
                    .crossfade(true)
                    .build(),
                contentDescription = "Profile photo",
                modifier = Modifier.fillMaxSize().clip(CircleShape),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        } else {
            GeneratedAvatar(seed = seed, initials = initials, size = size)
        }
    }
}
```

- [ ] **Step 2: Update SettingsScreen with photo picker**

Replace the entire `SettingsScreen.kt` content with:

```kotlin
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.carepulse.app.ui.screens.settings

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.carepulse.app.ui.components.ProfileAvatar
import com.carepulse.app.ui.components.PastelCard
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

    // Camera: stores output URI before launch
    var cameraOutputUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) cameraOutputUri?.let { vm.saveProfilePhoto(context, it) }
    }

    // Gallery
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { vm.saveProfilePhoto(context, it) }
    }

    // Camera permission
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
                    Text("Settings", style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary, fontWeight = FontWeight.Bold)
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
                    Box(
                        modifier = Modifier.clickable { showPhotoPicker = true }
                    ) {
                        ProfileAvatar(
                            photoPath = photoPath,
                            initials = initials,
                            seed = name.length,
                            size = 60
                        )
                        // Camera badge overlay
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(20.dp)
                                .androidx.compose.foundation.background(
                                    TealAccent,
                                    androidx.compose.foundation.shape.CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.AddAPhoto, null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                    Spacer(Modifier.size(14.dp))
                    Column {
                        Text(name, style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text(email, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        if (roleLabel.isNotEmpty()) {
                            Text(roleLabel, style = MaterialTheme.typography.labelLarge,
                                color = TealAccent, fontWeight = FontWeight.SemiBold)
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
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(26.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, null, tint = TextPrimary,
                    modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text("Sign out", color = TextPrimary, fontWeight = FontWeight.SemiBold)
            }

            Text("CarePulse v1.0.0", style = MaterialTheme.typography.bodySmall,
                color = TextSecondary, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
        }
    }

    // Photo picker bottom sheet
    if (showPhotoPicker) {
        ModalBottomSheet(
            onDismissRequest = { showPhotoPicker = false },
            sheetState = sheetState,
            containerColor = NavyPrimary
        ) {
            Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 32.dp)) {
                Text(
                    "Profile photo",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                PhotoSheetOption(
                    icon = Icons.Filled.CameraAlt,
                    label = "Take Photo"
                ) {
                    scope.launch { sheetState.hide() }.invokeOnCompletion { showPhotoPicker = false }
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
                PhotoSheetOption(
                    icon = Icons.Filled.Image,
                    label = "Choose from Gallery"
                ) {
                    scope.launch { sheetState.hide() }.invokeOnCompletion { showPhotoPicker = false }
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
        Icon(icon, null, tint = TealAccent, modifier = Modifier.size(24.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge,
            color = Color.White, fontWeight = FontWeight.Medium)
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
                color = TextPrimary, fontWeight = FontWeight.SemiBold)
            Text("Use this account as family, caregiver, or agency.",
                style = MaterialTheme.typography.bodySmall, color = TextSecondary)
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
                        style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
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
            Icon(icon, null, tint = TealAccent, modifier = Modifier.size(22.dp))
            Spacer(Modifier.size(14.dp))
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                Text(
                    when {
                        isActive   -> "Current mode"
                        isEnrolled -> "Tap to switch"
                        else       -> "Tap to add this role"
                    },
                    style = MaterialTheme.typography.bodySmall, color = TextSecondary
                )
            }
            if (isActive) {
                Icon(Icons.Filled.CheckCircle, null, tint = TealAccent,
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
        Icon(icon, null, tint = TealAccent, modifier = Modifier.size(22.dp))
        Spacer(Modifier.size(14.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary, modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null,
            tint = TextSecondary, modifier = Modifier.size(20.dp))
    }
}
```

- [ ] **Step 3: Commit**

```bash
cd /Users/ranjana/Harsha/CarePulse
git add app/src/main/java/com/carepulse/app/ui/components/CommonComponents.kt \
        app/src/main/java/com/carepulse/app/ui/screens/settings/SettingsScreen.kt
git commit -m "feat: profile photo picker with camera/gallery + ProfileAvatar composable"
```

---

## Task 5: Add `logVitals` to repository + ViewModel

**Files:**
- Modify: `app/src/main/java/com/carepulse/app/data/repository/CarePulseRepository.kt`
- Modify: `app/src/main/java/com/carepulse/app/data/repository/FirestoreCarePulseRepository.kt`
- Modify: `app/src/main/java/com/carepulse/app/data/repository/MockCarePulseRepository.kt`
- Modify: `app/src/main/java/com/carepulse/app/viewmodel/CarePulseViewModel.kt`

- [ ] **Step 1: Add `logVitals` to the repository interface**

In `CarePulseRepository.kt`, add after the `updateBookingStatus` line:

```kotlin
/** Write a standalone vitals snapshot (not tied to a shift report). */
suspend fun logVitals(vitals: VitalsLog)
```

- [ ] **Step 2: Implement `logVitals` in FirestoreCarePulseRepository**

In `FirestoreCarePulseRepository.kt`, add after the `updateBookingStatus` implementation:

```kotlin
override suspend fun logVitals(vitals: VitalsLog) {
    runCatching { vitalsCol.add(vitals.toMap()).await() }
}
```

- [ ] **Step 3: Implement `logVitals` in MockCarePulseRepository**

In `MockCarePulseRepository.kt`, add a no-op (or list append if mock vitals exist):

```kotlin
override suspend fun logVitals(vitals: VitalsLog) {
    // No-op for mock; real data comes from FirestoreCarePulseRepository
}
```

- [ ] **Step 4: Add `logVitals` to ViewModel**

In `CarePulseViewModel.kt`, add after the `submitShiftReport` or similar section (search for `fun submitShiftReport`):

```kotlin
fun logVitals(
    heartRate: Int,
    systolic: Int,
    diastolic: Int,
    mood: Mood,
    mealsEaten: Int,
    notes: String
) {
    viewModelScope.launch {
        val today = java.time.LocalDate.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("EEE, MMM d"))
        repo.logVitals(
            VitalsLog(
                dateLabel = today,
                heartRate = heartRate,
                bloodPressureSystolic = systolic,
                bloodPressureDiastolic = diastolic,
                mood = mood,
                mealsEaten = mealsEaten,
                notes = notes
            )
        )
    }
}
```

Add the import at the top of `CarePulseViewModel.kt`:
```kotlin
import java.time.LocalDate
import java.time.format.DateTimeFormatter
```

- [ ] **Step 5: Commit**

```bash
cd /Users/ranjana/Harsha/CarePulse
git add app/src/main/java/com/carepulse/app/data/repository/ \
        app/src/main/java/com/carepulse/app/viewmodel/CarePulseViewModel.kt
git commit -m "feat: add logVitals to repository interface + ViewModel"
```

---

## Task 6: New VitalsLogScreen (caregiver any-time vitals entry)

**Files:**
- Create: `app/src/main/java/com/carepulse/app/ui/screens/caregiver/VitalsLogScreen.kt`
- Modify: `app/src/main/java/com/carepulse/app/navigation/CarePulseNavGraph.kt`
- Modify: `app/src/main/java/com/carepulse/app/ui/screens/caregiver/CaregiverDashboardScreen.kt`

- [ ] **Step 1: Create VitalsLogScreen**

Create `app/src/main/java/com/carepulse/app/ui/screens/caregiver/VitalsLogScreen.kt`:

```kotlin
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
                    Text("Vitals", style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CarePulseTextField(
                            value = heartRate,
                            onValueChange = { heartRate = it.filter(Char::isDigit) },
                            label = "Heart rate (bpm)",
                            modifier = Modifier.weight(1f)
                        )
                    }
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
                    Text("Mood", style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary, fontWeight = FontWeight.SemiBold)
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
                    Text("Meals eaten: $meals / 3",
                        style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(0, 1, 2, 3).forEach { v ->
                            PastelChip("$v", selected = v == meals, onClick = { meals = v })
                        }
                    }
                }
            }

            PastelCard {
                Column {
                    Text("Notes", style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary, fontWeight = FontWeight.SemiBold)
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
```

- [ ] **Step 2: Add route constant in CarePulseNavGraph**

In `CarePulseNavGraph.kt`, inside `object Routes`, add:

```kotlin
const val VitalsLog = "vitals-log"
```

Then in the `NavHost` block, add before the closing `}`:

```kotlin
composable(Routes.VitalsLog) {
    VitalsLogScreen(
        vm = vm,
        onBack = { navController.popBackStack() }
    )
}
```

Also add the import:
```kotlin
import com.carepulse.app.ui.screens.caregiver.VitalsLogScreen
```

- [ ] **Step 3: Add "Log Vitals" button to CaregiverDashboardScreen**

In `CaregiverDashboardScreen.kt`, update the function signature to accept a new callback:

```kotlin
fun CaregiverDashboardScreen(
    vm: CarePulseViewModel,
    onClockOut: () -> Unit,
    onSignOut: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onLogVitals: () -> Unit = {}
)
```

Then inside the `Scaffold`, add a floating action button:

```kotlin
Scaffold(
    topBar = { /* existing */ },
    floatingActionButton = {
        androidx.compose.material3.ExtendedFloatingActionButton(
            onClick = onLogVitals,
            icon = { Icon(Icons.Filled.MonitorHeart, null) },
            text = { Text("Log Vitals") },
            containerColor = com.carepulse.app.ui.theme.TealAccent,
            contentColor = androidx.compose.ui.graphics.Color.White
        )
    },
    containerColor = Background
)
```

Add the import at top:
```kotlin
import androidx.compose.material.icons.filled.MonitorHeart
```

- [ ] **Step 4: Wire `onLogVitals` in NavGraph**

In `CarePulseNavGraph.kt`, update the `CaregiverDashboardScreen` call inside `composable(Routes.Home)`:

```kotlin
UserRole.CAREGIVER -> CaregiverDashboardScreen(
    vm = vm,
    onClockOut = { navController.navigate(Routes.ShiftSummary) },
    onSignOut = signOut,
    onEditProfile = { navController.navigate(Routes.EditProfile) },
    onLogVitals = { navController.navigate(Routes.VitalsLog) }
)
```

- [ ] **Step 5: Commit**

```bash
cd /Users/ranjana/Harsha/CarePulse
git add app/src/main/java/com/carepulse/app/ui/screens/caregiver/VitalsLogScreen.kt \
        app/src/main/java/com/carepulse/app/navigation/CarePulseNavGraph.kt \
        app/src/main/java/com/carepulse/app/ui/screens/caregiver/CaregiverDashboardScreen.kt
git commit -m "feat: add VitalsLogScreen for caregiver any-time vitals logging"
```

---

## Task 7: Enhanced Pulse Dashboard — BP chart + mood distribution + weekly stats

**Files:**
- Modify: `app/src/main/java/com/carepulse/app/ui/screens/customer/PulseDashboardScreen.kt`

- [ ] **Step 1: Replace PulseDashboardScreen with enhanced version**

Replace the entire content of `PulseDashboardScreen.kt` with the following. The key additions are:
1. `BpChart` — dual-line BP chart (systolic/diastolic)  
2. `MoodDistributionBar` — horizontal breakdown of last 7 moods  
3. Weekly stats summary strip (avg HR, avg BP, dominant mood)  
4. Count-up animation for vital numbers  

```kotlin
package com.carepulse.app.ui.screens.customer

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.VideoCall
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.carepulse.app.data.model.Mood
import com.carepulse.app.data.model.VitalsLog
import com.carepulse.app.ui.components.PastelCard
import com.carepulse.app.ui.components.PrimaryButton
import com.carepulse.app.ui.theme.Background
import com.carepulse.app.ui.theme.BorderLine
import com.carepulse.app.ui.theme.DangerRed
import com.carepulse.app.ui.theme.NavyPrimary
import com.carepulse.app.ui.theme.TealAccent
import com.carepulse.app.ui.theme.TealLight
import com.carepulse.app.ui.theme.TextPrimary
import com.carepulse.app.ui.theme.TextSecondary
import com.carepulse.app.viewmodel.CarePulseViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PulseDashboardScreen(
    vm: CarePulseViewModel,
    onVideoCall: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val vitalsList by vm.vitals.collectAsState()
    val reports by vm.reports.collectAsState()
    val today = vitalsList.firstOrNull()
    val last7 = vitalsList.take(7)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pulse Dashboard", color = TextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TextPrimary)
                        }
                    }
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            PatientStrip(name = "Mr. Lee", subtitle = "Admitted · Brookside Care", onVideoCall = onVideoCall)

            if (today != null) {
                // Today's vitals cards with count-up animation
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AnimatedVitalCard(
                        Modifier.weight(1f),
                        icon = Icons.Filled.Favorite,
                        title = "Heart rate",
                        targetValue = today.heartRate,
                        unit = "bpm",
                        accent = DangerRed
                    )
                    AnimatedVitalCard(
                        Modifier.weight(1f),
                        icon = Icons.Filled.MonitorHeart,
                        title = "Blood pressure",
                        targetValue = today.bloodPressureSystolic,
                        unit = "/${today.bloodPressureDiastolic} mmHg",
                        accent = TealAccent
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        Modifier.weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier.size(28.dp).clip(RoundedCornerShape(8.dp))
                                        .background(today.mood.color.copy(alpha = 0.4f)),
                                    contentAlignment = Alignment.Center
                                ) { Icon(today.mood.icon, null, tint = TextPrimary, modifier = Modifier.size(16.dp)) }
                                Spacer(Modifier.width(8.dp))
                                Text("Mood", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(today.mood.label, style = MaterialTheme.typography.headlineMedium,
                                color = TextPrimary, fontWeight = FontWeight.Bold)
                            Text("today", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        }
                    }
                    AnimatedVitalCard(
                        Modifier.weight(1f),
                        icon = Icons.Filled.Restaurant,
                        title = "Meals",
                        targetValue = today.mealsEaten,
                        unit = "/ 3 today",
                        accent = BorderLine
                    )
                }

                // --- Weekly Analytics ---
                WeeklyStatsStrip(last7)

                // Heart rate chart
                PastelCard {
                    Column {
                        Text("Heart rate — 7 days", style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        HeartRateChart(last7.map { it.heartRate.toFloat() }.reversed())
                    }
                }

                // BP chart
                if (last7.size >= 2) {
                    PastelCard {
                        Column {
                            Text("Blood pressure — 7 days", style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                ChartLegendDot(TealAccent, "Systolic")
                                ChartLegendDot(TealAccent.copy(alpha = 0.4f), "Diastolic")
                            }
                            Spacer(Modifier.height(8.dp))
                            BpChart(
                                systolic = last7.map { it.bloodPressureSystolic.toFloat() }.reversed(),
                                diastolic = last7.map { it.bloodPressureDiastolic.toFloat() }.reversed()
                            )
                        }
                    }
                }

                // Mood distribution
                if (last7.isNotEmpty()) {
                    PastelCard {
                        Column {
                            Text("Mood — last 7 entries", style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(10.dp))
                            MoodDistributionBar(last7)
                        }
                    }
                }
            }

            // Latest report
            reports.firstOrNull()?.let { report ->
                PastelCard {
                    Column {
                        Text("Shift Summary · ${report.dateLabel}",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        Text("By ${report.caregiverName}",
                            style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        Spacer(Modifier.height(8.dp))
                        Text(report.daySummary, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                        Spacer(Modifier.height(10.dp))
                        report.medicationsGiven.forEach { m ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier.size(10.dp).clip(RoundedCornerShape(5.dp))
                                        .background(if (m.administered) TealLight else BorderLine)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("${m.name} · ${m.dose}",
                                    style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun WeeklyStatsStrip(vitals: List<VitalsLog>) {
    if (vitals.isEmpty()) return
    val avgHr = vitals.map { it.heartRate }.average().roundToInt()
    val avgSys = vitals.map { it.bloodPressureSystolic }.average().roundToInt()
    val avgDia = vitals.map { it.bloodPressureDiastolic }.average().roundToInt()
    val topMood = vitals.groupBy { it.mood }.maxByOrNull { it.value.size }?.key

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatChip(Modifier.weight(1f), "Avg HR", "$avgHr bpm", DangerRed)
        StatChip(Modifier.weight(1f), "Avg BP", "$avgSys/$avgDia", TealAccent)
        if (topMood != null) {
            StatChip(Modifier.weight(1f), "Top mood", topMood.label, topMood.color)
        }
    }
}

@Composable
private fun StatChip(modifier: Modifier, label: String, value: String, accent: Color) {
    Box(
        modifier.clip(RoundedCornerShape(14.dp)).background(Color.White).padding(10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text(value, style = MaterialTheme.typography.titleSmall,
                color = accent, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ChartLegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
    }
}

@Composable
private fun MoodDistributionBar(vitals: List<VitalsLog>) {
    val total = vitals.size.toFloat()
    Mood.values().forEach { mood ->
        val count = vitals.count { it.mood == mood }
        if (count == 0) return@forEach
        val fraction = count / total
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(mood.icon, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
            Text(mood.label, style = MaterialTheme.typography.bodySmall,
                color = TextSecondary, modifier = Modifier.width(48.dp))
            Box(
                Modifier.weight(1f).height(10.dp).clip(RoundedCornerShape(5.dp))
                    .background(BorderLine)
            ) {
                Box(
                    Modifier.fillMaxWidth(fraction).height(10.dp)
                        .clip(RoundedCornerShape(5.dp)).background(mood.color)
                )
            }
            Text("$count", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}

@Composable
private fun AnimatedVitalCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    targetValue: Int,
    unit: String,
    accent: Color
) {
    val animatable = remember { Animatable(0f) }
    LaunchedEffect(targetValue) {
        animatable.animateTo(targetValue.toFloat(), animationSpec = tween(800))
    }
    val displayValue = animatable.value.roundToInt()

    Box(
        modifier.clip(RoundedCornerShape(20.dp)).background(Color.White).padding(14.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(28.dp).clip(RoundedCornerShape(8.dp))
                        .background(accent.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) { Icon(icon, null, tint = TextPrimary, modifier = Modifier.size(16.dp)) }
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
            Spacer(Modifier.height(8.dp))
            Text("$displayValue", style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary, fontWeight = FontWeight.Bold)
            Text(unit, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
    }
}

@Composable
private fun PatientStrip(name: String, subtitle: String, onVideoCall: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp))
            .background(TealLight).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        com.carepulse.app.ui.components.GeneratedAvatar(seed = name.hashCode(), initials = "ML", size = 56)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.titleLarge,
                color = TextPrimary, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = TextPrimary.copy(alpha = 0.75f))
        }
        IconButton(
            onClick = onVideoCall,
            modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.White)
        ) {
            Icon(Icons.Filled.VideoCall, null, tint = TextPrimary)
        }
    }
}

@Composable
private fun BpChart(systolic: List<Float>, diastolic: List<Float>) {
    if (systolic.isEmpty()) return
    val allValues = systolic + diastolic
    val min = allValues.min()
    val max = allValues.max().coerceAtLeast(min + 1f)

    var animated by remember { mutableStateOf(0f) }
    val progress by animateFloatAsState(targetValue = animated, animationSpec = tween(900), label = "bpChart")
    LaunchedEffect(Unit) { animated = 1f }

    val sysColor = TealAccent
    val diaColor = TealAccent.copy(alpha = 0.4f)

    Canvas(Modifier.fillMaxWidth().height(120.dp)) {
        val w = size.width
        val h = size.height
        val pad = 12f
        val stepX = (w - pad * 2) / (systolic.size - 1).coerceAtLeast(1)

        fun buildPath(values: List<Float>): Path {
            val p = Path()
            values.forEachIndexed { i, v ->
                val x = pad + stepX * i
                val y = h - pad - ((v - min) / (max - min)) * (h - pad * 2)
                if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
            }
            return p
        }

        clipRect(right = w * progress) {
            drawPath(buildPath(systolic), color = sysColor, style = Stroke(width = 5f))
            drawPath(buildPath(diastolic), color = diaColor, style = Stroke(width = 3f))
        }
    }
}

@Composable
private fun HeartRateChart(values: List<Float>) {
    if (values.isEmpty()) return
    val min = values.min()
    val max = values.max().coerceAtLeast(min + 1f)

    var animated by remember { mutableStateOf(0f) }
    val progress by animateFloatAsState(targetValue = animated, animationSpec = tween(900), label = "hrChart")
    LaunchedEffect(Unit) { animated = 1f }

    Canvas(Modifier.fillMaxWidth().height(140.dp)) {
        val w = size.width
        val h = size.height
        val padding = 12f
        val stepX = (w - padding * 2) / (values.size - 1).coerceAtLeast(1)
        val path = Path()
        values.forEachIndexed { i, v ->
            val x = padding + stepX * i
            val y = h - padding - ((v - min) / (max - min)) * (h - padding * 2)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        clipRect(right = w * progress) {
            drawPath(path = path, color = TealAccent, style = Stroke(width = 6f))
        }
        values.forEachIndexed { i, v ->
            val x = padding + stepX * i
            val y = h - padding - ((v - min) / (max - min)) * (h - padding * 2)
            drawCircle(color = TextPrimary, radius = 4f, center = Offset(x, y))
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
cd /Users/ranjana/Harsha/CarePulse
git add app/src/main/java/com/carepulse/app/ui/screens/customer/PulseDashboardScreen.kt
git commit -m "feat: enhance PulseDashboard with BP chart, mood distribution, weekly stats, count-up animation"
```

---

## Task 8: App-wide animations — nav transitions + stagger + button press

**Files:**
- Modify: `app/src/main/java/com/carepulse/app/navigation/CarePulseNavGraph.kt`
- Modify: `app/src/main/java/com/carepulse/app/ui/components/CommonComponents.kt`
- Modify: `app/src/main/java/com/carepulse/app/ui/screens/customer/CustomerDashboardScreen.kt`
- Modify: `app/src/main/java/com/carepulse/app/ui/screens/caregiver/CaregiverDashboardScreen.kt`

- [ ] **Step 1: Add animated nav transitions to NavHost**

In `CarePulseNavGraph.kt`, update the `NavHost(...)` call to add enter/exit transitions. Add these imports at the top:

```kotlin
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
```

Update the `NavHost` opening:

```kotlin
NavHost(
    navController = navController,
    startDestination = Routes.Splash,
    modifier = Modifier.padding(scaffoldPadding),
    enterTransition = {
        slideInHorizontally(initialOffsetX = { it / 4 }, animationSpec = tween(300)) +
        fadeIn(animationSpec = tween(300))
    },
    exitTransition = {
        fadeOut(animationSpec = tween(200))
    },
    popEnterTransition = {
        fadeIn(animationSpec = tween(300))
    },
    popExitTransition = {
        slideOutHorizontally(targetOffsetX = { it / 4 }, animationSpec = tween(300)) +
        fadeOut(animationSpec = tween(200))
    }
)
```

- [ ] **Step 2: Add animated PrimaryButton with press scale**

In `CommonComponents.kt`, add these imports:

```kotlin
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.ui.graphics.graphicsLayer
```

Replace the `PrimaryButton` composable with:

```kotlin
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.5f),
        label = "buttonScale"
    )
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale),
        enabled = enabled,
        shape = RoundedCornerShape(28.dp),
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = TealAccent,
            contentColor = Color.White,
            disabledContainerColor = TealAccent.copy(alpha = 0.4f)
        )
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}
```

- [ ] **Step 3: Add staggered card slide-in to CustomerDashboardScreen**

In `CustomerDashboardScreen.kt`, add these imports:

```kotlin
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
```

Wrap each main `Card` or `PastelCard` in the caregiver list `items {}` with:

```kotlin
var visible by remember { mutableStateOf(false) }
LaunchedEffect(caregiver.id) {
    delay(index * 60L)
    visible = true
}
AnimatedVisibility(
    visible = visible,
    enter = slideInVertically(
        initialOffsetY = { it / 3 },
        animationSpec = tween(350)
    ) + fadeIn(animationSpec = tween(350))
) {
    // existing caregiver card content
}
```

The `items(filtered) { caregiver ->` block already iterates — wrap the card body inside `AnimatedVisibility`. Add `index` to `items`:

```kotlin
items(filtered, key = { it.id }) { caregiver ->
```
Use `val index = filtered.indexOf(caregiver)` to get the position for the delay.

- [ ] **Step 4: Add staggered slide-in to CaregiverDashboardScreen shift cards**

In `CaregiverDashboardScreen.kt`, add the same imports and wrap `myShifts.forEachIndexed`:

```kotlin
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import kotlinx.coroutines.delay
```

For each shift card in the `forEach`/`forEachIndexed`, wrap with:

```kotlin
var visible by remember(shift.id) { mutableStateOf(false) }
LaunchedEffect(shift.id) {
    delay(index * 80L)
    visible = true
}
AnimatedVisibility(
    visible = visible,
    enter = slideInVertically(initialOffsetY = { it / 3 }, animationSpec = tween(350)) +
            fadeIn(animationSpec = tween(350))
) {
    // existing shift card
}
```

- [ ] **Step 5: Commit**

```bash
cd /Users/ranjana/Harsha/CarePulse
git add app/src/main/java/com/carepulse/app/navigation/CarePulseNavGraph.kt \
        app/src/main/java/com/carepulse/app/ui/components/CommonComponents.kt \
        app/src/main/java/com/carepulse/app/ui/screens/customer/CustomerDashboardScreen.kt \
        app/src/main/java/com/carepulse/app/ui/screens/caregiver/CaregiverDashboardScreen.kt
git commit -m "feat: add nav slide transitions, animated PrimaryButton, staggered card entrance"
```

---

## Task 9: Build + install

**Files:** (no file changes — verification only)

- [ ] **Step 1: Clean build**

```bash
cd /Users/ranjana/Harsha/CarePulse
./gradlew :app:assembleDebug 2>&1 | tail -20
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 2: Check for unresolved references**

```bash
cd /Users/ranjana/Harsha/CarePulse
./gradlew :app:assembleDebug 2>&1 | grep -i "error\|unresolved" | head -20
```
Expected: no errors printed.

- [ ] **Step 3: Install on tablet**

```bash
cd /Users/ranjana/Harsha/CarePulse
adb devices
./gradlew :app:installDebug
```
Expected: `BUILD SUCCESSFUL`, `Installed on ... devices`.

- [ ] **Step 4: Final commit + push**

```bash
cd /Users/ranjana/Harsha/CarePulse
git push origin feature/firebase-bottom-nav-polish
```

---

## Self-Review

**Spec coverage:**
- ✅ Profile photo via camera (Task 1, 2, 3, 4)
- ✅ Profile photo via gallery (Task 4)
- ✅ Caregiver any-time vitals log (Task 5, 6)
- ✅ Family sees BP chart (Task 7)
- ✅ Family sees mood distribution (Task 7)
- ✅ Weekly analytics summary (Task 7)
- ✅ Count-up vitals animation (Task 7 — `AnimatedVitalCard`)
- ✅ Nav screen transitions (Task 8)
- ✅ Button press scale (Task 8)
- ✅ Staggered card entrance (Task 8)
- ✅ Build + install (Task 9)

**Placeholder check:** None found.

**Type consistency:**
- `logVitals()` defined in Task 5 (interface, impl, ViewModel), called in Task 6 (VitalsLogScreen) ✅
- `profilePhotoPath: StateFlow<String?>` defined in Task 3, consumed in Task 4 ✅
- `ProfileAvatar` composable defined in Task 4 step 1, used in same task step 2 ✅
- `Routes.VitalsLog` defined in Task 6, wired in NavGraph in same task ✅
- `AnimatedVitalCard` defined and used within Task 7 ✅
