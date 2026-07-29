package com.carepulse.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Modernized type scale — larger display, tighter tracking for headings. */
val CarePulseTypography = Typography(
    displayLarge   = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold,     fontSize = 44.sp, lineHeight = 52.sp, letterSpacing = (-0.75).sp),
    displayMedium  = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold,     fontSize = 38.sp, lineHeight = 46.sp, letterSpacing = (-0.5).sp),
    displaySmall   = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold,     fontSize = 34.sp, lineHeight = 42.sp, letterSpacing = (-0.5).sp),
    headlineLarge  = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold,     fontSize = 30.sp, lineHeight = 38.sp, letterSpacing = (-0.25).sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold,     fontSize = 28.sp, lineHeight = 36.sp, letterSpacing = (-0.25).sp),
    headlineSmall  = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 28.sp),
    titleLarge     = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium    = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium,   fontSize = 16.sp, lineHeight = 22.sp),
    titleSmall     = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium,   fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge      = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal,   fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium     = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal,   fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall      = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal,   fontSize = 13.sp, lineHeight = 18.sp),
    labelLarge     = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, letterSpacing = 0.25.sp),
    labelMedium    = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium,   fontSize = 12.sp, letterSpacing = 0.5.sp),
    labelSmall     = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 0.5.sp)
)
