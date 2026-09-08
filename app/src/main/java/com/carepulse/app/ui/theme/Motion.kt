package com.carepulse.app.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing

object Motion {
    // Material 3 motion durations
    const val DurationShort = 150
    const val DurationMedium = 300
    const val DurationLong = 500
    // Glass-on-grid motion durations (ms), named per the design handoff's moment table
    const val ScreenEnter = 340
    const val ListStaggerStep = 60
    const val ListStaggerMax = 8
    const val ListItemDuration = 500
    const val ProfileHero = 460
    const val BottomSheet = 380
    const val FabToSheet = 380
    const val SheetScrim = 220
    const val LiveShiftPulse = 2200
    const val LiveShiftPulseOffset = 1100
    const val HeartBeat = 1800
    const val TabIndicator = 340
    const val PressFeedback = 140
    const val PullToRefreshTurn = 900
    const val SparklineDraw = 1300
    const val RevenueBars = 600
    const val ThemeSwitch = 280

    // Material 3 easing curves
    val Standard: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val Emphasized: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val EmphasizedDecelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    val EmphasizedAccelerate: Easing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
}
