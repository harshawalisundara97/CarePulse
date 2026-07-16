package com.carepulse.app.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import com.carepulse.app.R
import com.carepulse.app.ui.components.GradientHeader
import com.carepulse.app.ui.theme.AppLanguage
import com.carepulse.app.ui.theme.LanguagePreference

@Composable
fun LanguagePickerScreen(onBack: () -> Unit) {
    var selected by remember { mutableStateOf(LanguagePreference.current()) }

    Column(Modifier.fillMaxSize()) {
        GradientHeader(
            title = stringResource(R.string.settings_language),
            subtitle = null
        )
        Spacer(Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            LanguageRow(AppLanguage.ENGLISH, R.string.settings_language_english, selected) {
                selected = it; LanguagePreference.set(it)
            }
            LanguageRow(AppLanguage.SINHALA, R.string.settings_language_sinhala, selected) {
                selected = it; LanguagePreference.set(it)
            }
            LanguageRow(AppLanguage.TAMIL, R.string.settings_language_tamil, selected) {
                selected = it; LanguagePreference.set(it)
            }
        }
    }
}

@Composable
private fun LanguageRow(
    lang: AppLanguage,
    labelRes: Int,
    selected: AppLanguage,
    onSelect: (AppLanguage) -> Unit
) {
    Row(
        Modifier
            .fillMaxSize()
            .selectable(selected = lang == selected, onClick = { onSelect(lang) })
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = lang == selected, onClick = { onSelect(lang) })
        Spacer(Modifier.height(0.dp))
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}
