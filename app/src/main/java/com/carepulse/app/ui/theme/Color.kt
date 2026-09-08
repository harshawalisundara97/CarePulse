package com.carepulse.app.ui.theme

import androidx.compose.ui.graphics.Color

// Accent — primary actions, active nav, accent field block, key figures
val AccentPrimary     = Color(0xFFEC3013)
val AccentPrimaryDark = Color(0xFFFF563C)
val AccentPressed     = Color(0xFFAE1800)
val AccentPressedDark = Color(0xFFFF7A66)

// Text
val TextPrimary     = Color(0xFF201E1D)
val TextPrimaryDark = Color(0xFFF4F2F1)
val TextMuted       = Color(0xFF201E1D).copy(alpha = 0.55f)
val TextMutedDark   = Color(0xFFF4F2F1).copy(alpha = 0.55f)

// Ground
val Background      = Color(0xFFF3F2F2)
val BackgroundDark   = Color(0xFF141312)
val GroundDeep       = Color(0xFFE6E3E1)
val GroundDeepDark   = Color(0xFF0D0C0C)

// Glass surfaces
val GlassFill        = Color(0xFFFFFFFF).copy(alpha = 0.62f)
val GlassFillDark     = Color(0xFFFFFFFF).copy(alpha = 0.07f)
val GlassFillSubtle   = Color(0xFFFFFFFF).copy(alpha = 0.38f)
val GlassFillSubtleDark = Color(0xFFFFFFFF).copy(alpha = 0.04f)
val GlassBorder       = Color(0xFFFFFFFF).copy(alpha = 0.85f)
val GlassBorderDark   = Color(0xFFFFFFFF).copy(alpha = 0.13f)

// Rules / grid lines
val Rule     = Color(0xFF201E1D).copy(alpha = 0.14f)
val RuleDark = Color(0xFFFFFFFF).copy(alpha = 0.11f)

// Elevation tint (never pure black)
val ShadowTint     = Color(0xFF2D2B2B).copy(alpha = 0.13f)
val ShadowTintDark = Color(0xFF000000).copy(alpha = 0.50f)

// Status — same hue in both themes
val StatusAvailable  = Color(0xFF16A34A)
val StatusOnDuty     = Color(0xFFF97316)
val StatusOnDutyText = Color(0xFFC2410C)
val StatusDueFill    = Color(0xFFEC3013).copy(alpha = 0.14f)
val StatusDueText    = Color(0xFFAE1800)

// General semantic (kept for existing error/success call sites across all roles)
val DangerRed    = Color(0xFFDC2626)
val SuccessGreen = Color(0xFF16A34A)
