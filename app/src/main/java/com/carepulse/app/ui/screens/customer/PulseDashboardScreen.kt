package com.carepulse.app.ui.screens.customer

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material.icons.filled.WaterDrop
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.carepulse.app.data.model.VitalsLog
import com.carepulse.app.ui.components.PastelCard
import com.carepulse.app.ui.components.PrimaryButton
import com.carepulse.app.ui.theme.CreamBackground
import com.carepulse.app.ui.theme.InkPrimary
import com.carepulse.app.ui.theme.InkSecondary
import com.carepulse.app.ui.theme.PastelMint
import com.carepulse.app.ui.theme.PulseRed
import com.carepulse.app.ui.theme.SoftLavender
import com.carepulse.app.ui.theme.SoftPeach
import com.carepulse.app.viewmodel.CarePulseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PulseDashboardScreen(
    vm: CarePulseViewModel,
    onVideoCall: () -> Unit,
    onBack: () -> Unit
) {
    val vitalsList by vm.vitals.collectAsState()
    val reports by vm.reports.collectAsState()
    val today = vitalsList.firstOrNull()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pulse Dashboard", color = InkPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Patient strip
            PatientStrip(name = "Mr. Lee", subtitle = "Admitted · Brookside Care", onVideoCall = onVideoCall)

            if (today != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    VitalCard(
                        Modifier.weight(1f),
                        icon = Icons.Filled.Favorite,
                        title = "Heart rate",
                        value = "${today.heartRate}",
                        unit = "bpm",
                        accent = PulseRed
                    )
                    VitalCard(
                        Modifier.weight(1f),
                        icon = Icons.Filled.MonitorHeart,
                        title = "Blood pressure",
                        value = "${today.bloodPressureSystolic}/${today.bloodPressureDiastolic}",
                        unit = "mmHg",
                        accent = SoftLavender
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    VitalCard(
                        Modifier.weight(1f),
                        icon = today.mood.icon,
                        title = "Mood",
                        value = today.mood.label,
                        unit = "today",
                        accent = today.mood.color
                    )
                    VitalCard(
                        Modifier.weight(1f),
                        icon = Icons.Filled.Restaurant,
                        title = "Meals",
                        value = "${today.mealsEaten}/3",
                        unit = "today",
                        accent = SoftPeach
                    )
                }

                PastelCard {
                    Column {
                        Text("Heart rate this week", style = MaterialTheme.typography.titleMedium,
                            color = InkPrimary, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        HeartRateChart(vitalsList.take(7).map { it.heartRate.toFloat() }.reversed())
                    }
                }
            }

            // Latest report
            reports.firstOrNull()?.let { report ->
                PastelCard {
                    Column {
                        Text("Shift Summary · ${report.dateLabel}",
                            style = MaterialTheme.typography.titleMedium,
                            color = InkPrimary, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        Text("By ${report.caregiverName}",
                            style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
                        Spacer(Modifier.height(8.dp))
                        Text(report.daySummary, style = MaterialTheme.typography.bodyLarge, color = InkPrimary)
                        Spacer(Modifier.height(10.dp))
                        report.medicationsGiven.forEach { m ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier
                                        .size(10.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(if (m.administered) PastelMint else SoftPeach)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("${m.name} · ${m.dose}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = InkPrimary)
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
private fun PatientStrip(name: String, subtitle: String, onVideoCall: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.horizontalGradient(listOf(SoftLavender, PastelMint)))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        com.carepulse.app.ui.components.GeneratedAvatar(seed = name.hashCode(), initials = "ML", size = 56)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.titleLarge,
                color = InkPrimary, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = InkPrimary.copy(alpha = 0.75f))
        }
        IconButton(
            onClick = onVideoCall,
            modifier = Modifier
                .size(48.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(Color.White)
        ) {
            Icon(Icons.Filled.VideoCall, null, tint = InkPrimary)
        }
    }
}

@Composable
private fun VitalCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    value: String,
    unit: String,
    accent: Color
) {
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
                    Icon(icon, null, tint = InkPrimary, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
            }
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium,
                color = InkPrimary, fontWeight = FontWeight.Bold)
            Text(unit, style = MaterialTheme.typography.bodyMedium, color = InkSecondary)
        }
    }
}

@Composable
private fun HeartRateChart(values: List<Float>) {
    if (values.isEmpty()) return
    val min = values.min()
    val max = values.max().coerceAtLeast(min + 1f)

    var animated by remember { mutableStateOf(0f) }
    val progress by animateFloatAsState(targetValue = animated, animationSpec = tween(900), label = "chart")
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

        // Animated reveal by clipping width
        clipRect(right = w * progress) {
            drawPath(
                path = path,
                brush = Brush.horizontalGradient(listOf(PulseRed, PastelMint)),
                style = Stroke(width = 6f)
            )
        }
        // Points
        values.forEachIndexed { i, v ->
            val x = padding + stepX * i
            val y = h - padding - ((v - min) / (max - min)) * (h - padding * 2)
            drawCircle(color = InkPrimary, radius = 4f, center = Offset(x, y))
        }
    }
}
