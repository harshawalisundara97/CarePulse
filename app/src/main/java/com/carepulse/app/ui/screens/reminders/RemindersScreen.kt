@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.carepulse.app.ui.screens.reminders

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MedicationLiquid
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.carepulse.app.R
import com.carepulse.app.data.reminders.MedicationReminder
import com.carepulse.app.data.reminders.ReminderRepository
import com.carepulse.app.ui.components.CarePulseTextField
import com.carepulse.app.ui.components.GradientHeader
import com.carepulse.app.ui.components.PastelCard
import com.carepulse.app.ui.components.PrimaryButton
import com.carepulse.app.util.Haptics
import kotlinx.coroutines.launch

@Composable
fun RemindersScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { ReminderRepository(context) }
    val reminders by repository.reminders.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var showAddSheet by remember { mutableStateOf(false) }

    val notifPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* no-op: rationale shown inline if denied */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddSheet = true }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.reminders_add))
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            GradientHeader(title = stringResource(R.string.reminders_title))
            if (reminders.isEmpty()) {
                Column(
                    Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Filled.MedicationLiquid, contentDescription = null,
                        modifier = Modifier.height(48.dp), tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        stringResource(R.string.reminders_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(reminders, key = { it.id }) { reminder ->
                        ReminderRow(reminder) {
                            scope.launch {
                                repository.remove(reminder)
                                Haptics.performTap(context)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        AddReminderSheet(
            onDismiss = { showAddSheet = false },
            onSave = { name, dosage, hour, minute ->
                scope.launch {
                    repository.add(name, dosage, hour, minute)
                    Haptics.performSuccess(context)
                }
                showAddSheet = false
            }
        )
    }
}

@Composable
private fun ReminderRow(reminder: MedicationReminder, onDelete: () -> Unit) {
    PastelCard {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(reminder.name, style = MaterialTheme.typography.titleMedium)
                val timeLabel = "%02d:%02d".format(reminder.hour, reminder.minute)
                val subtitle = if (reminder.dosage.isBlank()) timeLabel else "$timeLabel • ${reminder.dosage}"
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.common_delete))
            }
        }
    }
}

@Composable
private fun AddReminderSheet(
    onDismiss: () -> Unit,
    onSave: (name: String, dosage: String, hour: Int, minute: Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    val timePickerState = rememberTimePickerState(is24Hour = false)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.fillMaxWidth().padding(20.dp)) {
            Text(stringResource(R.string.reminders_add), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            CarePulseTextField(value = name, onValueChange = { name = it }, label = stringResource(R.string.reminders_med_name))
            Spacer(Modifier.height(12.dp))
            CarePulseTextField(value = dosage, onValueChange = { dosage = it }, label = stringResource(R.string.reminders_dosage))
            Spacer(Modifier.height(16.dp))
            TimePicker(state = timePickerState)
            Spacer(Modifier.height(16.dp))
            PrimaryButton(
                text = stringResource(R.string.common_save),
                enabled = name.isNotBlank(),
                onClick = { onSave(name.trim(), dosage.trim(), timePickerState.hour, timePickerState.minute) }
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}
