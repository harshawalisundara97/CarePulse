package com.carepulse.app.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing

object Motion {
    // Material 3 motion durations
    const val DurationShort = 150
    const val DurationMedium = 300
    const val DurationLong = 500

    // Material 3 easing curves
    val Standard: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val Emphasized: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val EmphasizedDecelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    val EmphasizedAccelerate: Easing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
}
