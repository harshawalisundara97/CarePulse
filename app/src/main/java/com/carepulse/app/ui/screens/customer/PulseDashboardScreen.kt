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
import com.carepulse.app.ui.components.GeneratedAvatar
import com.carepulse.app.ui.theme.Background
import com.carepulse.app.ui.theme.BorderLine
import com.carepulse.app.ui.theme.DangerRed
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
                // Animated vital cards with count-up
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
                    // Mood card (static — mood is categorical)
                    Box(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .padding(14.dp)
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
                                        tint = TextPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Text("Mood", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                today.mood.label,
                                style = MaterialTheme.typography.headlineMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
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

                // Weekly summary stats
                WeeklyStatsStrip(last7)

                // Heart rate trend chart
                PastelCard {
                    Column {
                        Text(
                            "Heart rate — 7 days",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(8.dp))
                        HeartRateChart(last7.map { it.heartRate.toFloat() }.reversed())
                    }
                }

                // Blood pressure trend chart
                if (last7.size >= 2) {
                    PastelCard {
                        Column {
                            Text(
                                "Blood pressure — 7 days",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
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
                            Text(
                                "Mood — last 7 entries",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(10.dp))
                            MoodDistributionBar(last7)
                        }
                    }
                }
            }

            // Latest shift report
            reports.firstOrNull()?.let { report ->
                PastelCard {
                    Column {
                        Text(
                            "Shift Summary · ${report.dateLabel}",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "By ${report.caregiverName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(report.daySummary, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
                        Spacer(Modifier.height(10.dp))
                        report.medicationsGiven.forEach { m ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier
                                        .size(10.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(if (m.administered) TealLight else BorderLine)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "${m.name} · ${m.dose}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

// ── Weekly stats strip ────────────────────────────────────────────────────────

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
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text(
                value,
                style = MaterialTheme.typography.titleSmall,
                color = accent,
                fontWeight = FontWeight.Bold
            )
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
            Icon(mood.icon, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
            Text(
                mood.label,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.width(48.dp)
            )
            Box(
                Modifier
                    .weight(1f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(BorderLine)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(fraction)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(mood.color)
                )
            }
            Text("$count", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
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
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
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
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(14.dp)
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
                    Icon(icon, null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "$displayValue",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(unit, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        }
    }
}

@Composable
private fun PatientStrip(name: String, subtitle: String, onVideoCall: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(TealLight)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GeneratedAvatar(seed = name.hashCode(), initials = "ML", size = 56)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                name,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary.copy(alpha = 0.75f)
            )
        }
        IconButton(
            onClick = onVideoCall,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White)
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
    val progress by animateFloatAsState(
        targetValue = animated,
        animationSpec = tween(900),
        label = "bpChart"
    )
    LaunchedEffect(Unit) { animated = 1f }

    Canvas(
        Modifier
            .fillMaxWidth()
            .height(120.dp)
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

        clipRect(right = w * progress) {
            drawPath(buildPath(systolic), color = TealAccent, style = Stroke(width = 5f))
            drawPath(buildPath(diastolic), color = TealAccent.copy(alpha = 0.4f), style = Stroke(width = 3f))
        }
    }
}

@Composable
private fun HeartRateChart(values: List<Float>) {
    if (values.isEmpty()) return
    val min = values.min()
    val max = values.max().coerceAtLeast(min + 1f)

    var animated by remember { mutableStateOf(0f) }
    val progress by animateFloatAsState(
        targetValue = animated,
        animationSpec = tween(900),
        label = "hrChart"
    )
    LaunchedEffect(Unit) { animated = 1f }

    Canvas(
        Modifier
            .fillMaxWidth()
            .height(140.dp)
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
