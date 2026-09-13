package com.carepulse.app.ui.screens.customer

import android.provider.Settings
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.carepulse.app.R
import com.carepulse.app.data.model.Mood
import com.carepulse.app.data.model.VitalsLog
import com.carepulse.app.ui.components.GeneratedAvatar
import com.carepulse.app.ui.theme.DangerRed
import com.carepulse.app.ui.theme.GlassScreen
import com.carepulse.app.ui.theme.Motion
import com.carepulse.app.ui.theme.Radii
import com.carepulse.app.ui.theme.Spacing
import com.carepulse.app.ui.theme.TypeLabel
import com.carepulse.app.ui.theme.TypeNumericM
import com.carepulse.app.ui.theme.glassCard
import com.carepulse.app.viewmodel.CarePulseViewModel
import dev.chrisbanes.haze.HazeState
import kotlin.math.roundToInt

/**
 * True when the device has animations turned off system-wide (developer options or an
 * accessibility preference sets the animator duration scale to 0). Animation-heavy surfaces
 * must degrade to a static/faded state rather than animating when this is true.
 */
@Composable
private fun rememberReduceMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) == 0f
    }
}

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

    GlassScreen { hazeState ->
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.ScreenPaddingCompact),
            verticalArrangement = Arrangement.spacedBy(Spacing.CardGap)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.CardGap),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBack != null) {
                    IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                }
                Text(
                    stringResource(R.string.pulse_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            PatientStrip(
                name = "Mr. Lee",
                subtitle = "Admitted · Brookside Care",
                onVideoCall = onVideoCall,
                hazeState = hazeState
            )

            if (today != null) {
                // Animated vital cards with count-up
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.ItemGap)) {
                    AnimatedVitalCard(
                        Modifier.weight(1f),
                        icon = Icons.Filled.Favorite,
                        title = stringResource(R.string.vitals_heart_rate),
                        targetValue = today.heartRate,
                        unit = "bpm",
                        accent = DangerRed,
                        hazeState = hazeState
                    )
                    AnimatedVitalCard(
                        Modifier.weight(1f),
                        icon = Icons.Filled.MonitorHeart,
                        title = stringResource(R.string.vitals_blood_pressure),
                        targetValue = today.bloodPressureSystolic,
                        unit = "/${today.bloodPressureDiastolic} mmHg",
                        accent = MaterialTheme.colorScheme.primary,
                        hazeState = hazeState
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.ItemGap)) {
                    // Mood card (static — mood is categorical)
                    Box(
                        Modifier
                            .weight(1f)
                            .glassCard(Radii.StatTile, hazeState)
                            .padding(Spacing.CardPaddingCompact)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(today.mood.color.copy(alpha = 0.4f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        today.mood.icon, null,
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    stringResource(R.string.vitals_mood),
                                    style = TypeLabel,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                today.mood.label,
                                style = TypeNumericM,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                stringResource(R.string.pulse_today),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    AnimatedVitalCard(
                        Modifier.weight(1f),
                        icon = Icons.Filled.Restaurant,
                        title = stringResource(R.string.pulse_meals),
                        targetValue = today.mealsEaten,
                        unit = stringResource(R.string.pulse_meals_unit),
                        accent = MaterialTheme.colorScheme.primary,
                        hazeState = hazeState
                    )
                }

                // Weekly summary stats
                WeeklyStatsStrip(last7, hazeState)

                // Heart rate trend chart
                Box(
                    Modifier
                        .fillMaxWidth()
                        .glassCard(Radii.Card, hazeState)
                        .padding(Spacing.CardPaddingCompact)
                ) {
                    Column {
                        Text(
                            stringResource(R.string.pulse_hr_7_days),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(8.dp))
                        HeartRateChart(last7.map { it.heartRate.toFloat() }.reversed())
                    }
                }

                // Blood pressure trend chart
                if (last7.size >= 2) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .glassCard(Radii.Card, hazeState)
                            .padding(Spacing.CardPaddingCompact)
                    ) {
                        Column {
                            Text(
                                stringResource(R.string.pulse_bp_7_days),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                ChartLegendDot(
                                    MaterialTheme.colorScheme.primary,
                                    stringResource(R.string.pulse_systolic)
                                )
                                ChartLegendDot(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                    stringResource(R.string.pulse_diastolic)
                                )
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
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .glassCard(Radii.Card, hazeState)
                            .padding(Spacing.CardPaddingCompact)
                    ) {
                        Column {
                            Text(
                                stringResource(R.string.pulse_mood_7_entries),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(10.dp))
                            MoodDistributionBar(last7)
                        }
                    }
                }
            }

            // Latest shift report
            reports.firstOrNull()?.let { report ->
                Box(
                    Modifier
                        .fillMaxWidth()
                        .glassCard(Radii.Card, hazeState)
                        .padding(Spacing.CardPaddingCompact)
                ) {
                    Column {
                        Text(
                            "${stringResource(R.string.pulse_shift_summary)} · ${report.dateLabel}",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${stringResource(R.string.pulse_by)} ${report.caregiverName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            report.daySummary,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(10.dp))
                        report.medicationsGiven.forEach { m ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (m.administered) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.outline
                                        )
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "${m.name} · ${m.dose} · " + stringResource(
                                        if (m.administered) R.string.pulse_med_given
                                        else R.string.pulse_med_not_given
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(Spacing.SectionSpacingCompact))
        }
    }
}

// ── Weekly stats strip ────────────────────────────────────────────────────────

@Composable
private fun WeeklyStatsStrip(vitals: List<VitalsLog>, hazeState: HazeState) {
    if (vitals.isEmpty()) return
    val avgHr = vitals.map { it.heartRate }.average().roundToInt()
    val avgSys = vitals.map { it.bloodPressureSystolic }.average().roundToInt()
    val avgDia = vitals.map { it.bloodPressureDiastolic }.average().roundToInt()
    val topMood = vitals.groupBy { it.mood }.maxByOrNull { it.value.size }?.key

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.ItemGap)
    ) {
        StatChip(Modifier.weight(1f), stringResource(R.string.pulse_avg_hr), "$avgHr bpm", hazeState)
        StatChip(Modifier.weight(1f), stringResource(R.string.pulse_avg_bp), "$avgSys/$avgDia", hazeState)
        if (topMood != null) {
            StatChip(Modifier.weight(1f), stringResource(R.string.pulse_top_mood), topMood.label, hazeState)
        }
    }
}

@Composable
private fun StatChip(modifier: Modifier, label: String, value: String, hazeState: HazeState) {
    Box(
        modifier
            .glassCard(Radii.StatTile, hazeState)
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = TypeLabel, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(2.dp))
            Text(value, style = TypeNumericM, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

// ── Mood distribution ─────────────────────────────────────────────────────────

@Composable
private fun MoodDistributionBar(vitals: List<VitalsLog>) {
    val total = vitals.size.toFloat()
    Mood.values().forEach { mood ->
        val count = vitals.count { it.mood == mood }
        if (count == 0) return@forEach
        val fraction = count / total
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(mood.icon, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
            Text(
                mood.label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.width(48.dp)
            )
            Box(
                Modifier
                    .weight(1f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(MaterialTheme.colorScheme.outline)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(fraction)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(mood.color)
                )
            }
            Text("$count", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

// ── Charts ────────────────────────────────────────────────────────────────────

@Composable
private fun ChartLegendDot(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun AnimatedVitalCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    targetValue: Int,
    unit: String,
    accent: Color,
    hazeState: HazeState
) {
    val reduceMotion = rememberReduceMotion()
    val animatable = remember { Animatable(0f) }
    LaunchedEffect(targetValue, reduceMotion) {
        if (reduceMotion) {
            animatable.snapTo(targetValue.toFloat())
        } else {
            animatable.animateTo(targetValue.toFloat(), animationSpec = tween(800))
        }
    }
    val displayValue = animatable.value.roundToInt()

    Box(
        modifier
            .glassCard(Radii.StatTile, hazeState)
            .padding(Spacing.CardPaddingCompact)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accent.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text(title, style = TypeLabel, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.height(8.dp))
            Text("$displayValue", style = TypeNumericM, color = MaterialTheme.colorScheme.onSurface)
            Text(unit, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun PatientStrip(
    name: String,
    subtitle: String,
    onVideoCall: () -> Unit,
    hazeState: HazeState
) {
    Row(
        Modifier
            .fillMaxWidth()
            .glassCard(Radii.CardLarge, hazeState)
            .padding(Spacing.CardPaddingCompact),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GeneratedAvatar(seed = name.hashCode(), initials = "ML", size = 56)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        IconButton(
            onClick = onVideoCall,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        ) {
            Icon(
                Icons.Filled.VideoCall,
                contentDescription = stringResource(R.string.pulse_video_call),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun BpChart(systolic: List<Float>, diastolic: List<Float>) {
    if (systolic.isEmpty()) return
    val allValues = systolic + diastolic
    val min = allValues.min()
    val max = allValues.max().coerceAtLeast(min + 1f)

    val reduceMotion = rememberReduceMotion()
    var animated by remember { mutableStateOf(0f) }
    val progress by animateFloatAsState(
        targetValue = animated,
        animationSpec = tween(
            durationMillis = Motion.SparklineDraw,
            easing = Motion.Emphasized
        ),
        label = "bpChart"
    )
    LaunchedEffect(Unit) { animated = 1f }
    val drawProgress = if (reduceMotion) 1f else progress
    val lineColor = MaterialTheme.colorScheme.primary
    val description = stringResource(
        R.string.pulse_bp_chart_description,
        systolic.size,
        systolic.last().roundToInt(),
        diastolic.last().roundToInt()
    )

    Canvas(
        Modifier
            .fillMaxWidth()
            .height(120.dp)
            .semantics { contentDescription = description }
    ) {
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

        clipRect(right = w * drawProgress) {
            drawPath(buildPath(systolic), color = lineColor, style = Stroke(width = 5f))
            drawPath(buildPath(diastolic), color = lineColor.copy(alpha = 0.4f), style = Stroke(width = 3f))
        }
    }
}

@Composable
private fun HeartRateChart(values: List<Float>) {
    if (values.isEmpty()) return
    val min = values.min()
    val max = values.max().coerceAtLeast(min + 1f)

    val reduceMotion = rememberReduceMotion()
    var animated by remember { mutableStateOf(0f) }
    val progress by animateFloatAsState(
        targetValue = animated,
        animationSpec = tween(
            durationMillis = Motion.SparklineDraw,
            easing = Motion.Emphasized
        ),
        label = "hrChart"
    )
    LaunchedEffect(Unit) { animated = 1f }
    val drawProgress = if (reduceMotion) 1f else progress
    val pointColor = MaterialTheme.colorScheme.onSurface
    val hrLineColor = MaterialTheme.colorScheme.primary
    val description = stringResource(
        R.string.pulse_hr_chart_description,
        values.size,
        min.roundToInt(),
        max.roundToInt()
    )

    Canvas(
        Modifier
            .fillMaxWidth()
            .height(140.dp)
            .semantics { contentDescription = description }
    ) {
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
        clipRect(right = w * drawProgress) {
            drawPath(path = path, color = hrLineColor, style = Stroke(width = 6f))
        }
        values.forEachIndexed { i, v ->
            val x = padding + stepX * i
            val y = h - padding - ((v - min) / (max - min)) * (h - padding * 2)
            drawCircle(color = pointColor, radius = 4f, center = Offset(x, y))
        }
    }
}
