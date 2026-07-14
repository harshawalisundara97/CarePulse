package com.carepulse.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Context.vitalsWidgetStore by preferencesDataStore(name = "vitals_widget")
private val KEY_HR = stringPreferencesKey("hr")
private val KEY_BP = stringPreferencesKey("bp")
private val KEY_MOOD = stringPreferencesKey("mood")

class VitalsWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = context.vitalsWidgetStore.data
        provideContent {
            val data = androidx.glance.currentState<androidx.datastore.preferences.core.Preferences>()
            val hr = data[KEY_HR] ?: "—"
            val bp = data[KEY_BP] ?: "—"
            val mood = data[KEY_MOOD] ?: "—"
            WidgetBody(hr = hr, bp = bp, mood = mood)
        }
    }
}

@Composable
private fun WidgetBody(hr: String, bp: String, mood: String) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color(0xFF0D9488)))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.Start
    ) {
        Text("Vitals",
            style = TextStyle(color = ColorProvider(Color.White),
                fontSize = 14.sp, fontWeight = FontWeight.Bold))
        Spacer(GlanceModifier.height(6.dp))
        Text("HR: $hr bpm", style = TextStyle(color = ColorProvider(Color.White), fontSize = 12.sp))
        Text("BP: $bp",      style = TextStyle(color = ColorProvider(Color.White), fontSize = 12.sp))
        Text("Mood: $mood",  style = TextStyle(color = ColorProvider(Color.White), fontSize = 12.sp))
    }
}

object VitalsWidgetUpdater {
    suspend fun update(context: Context, hr: String?, bp: String?, mood: String?) {
        context.vitalsWidgetStore.edit { prefs ->
            hr?.let { prefs[KEY_HR] = it }
            bp?.let { prefs[KEY_BP] = it }
            mood?.let { prefs[KEY_MOOD] = it }
        }
        VitalsWidget().updateAll(context)
    }
}
