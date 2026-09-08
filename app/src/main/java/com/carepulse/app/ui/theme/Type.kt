package com.carepulse.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.carepulse.app.R

/** Archivo — required for the glass-on-grid design system. Do not substitute. */
val Archivo = FontFamily(
    Font(R.font.archivo_regular, FontWeight.Normal),
    Font(R.font.archivo_semibold, FontWeight.SemiBold),
    Font(R.font.archivo_bold, FontWeight.Bold),
    Font(R.font.archivo_extrabold, FontWeight.ExtraBold)
)

/** Sizes with no Material 3 Typography slot — used directly via these vals. */
val TypeDisplay = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 46.sp, letterSpacing = (-1.38).sp)
val TypeNumericXl = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 44.sp, letterSpacing = (-1.32).sp)
val TypeNumericL = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, letterSpacing = (-0.64).sp)
val TypeNumericM = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 23.sp, letterSpacing = (-0.46).sp)
val TypeLabel = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, letterSpacing = 0.6.sp)
val TypeChip = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
val TypeNav = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 9.5.sp, letterSpacing = 0.19.sp)

/** Glass-on-grid type scale, mapped onto Material 3's Typography slots. */
val CarePulseTypography = Typography(
    displayLarge   = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 46.sp, lineHeight = 52.sp, letterSpacing = (-1.38).sp),
    displayMedium  = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 38.sp, lineHeight = 44.sp, letterSpacing = (-1.14).sp),
    displaySmall   = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp, lineHeight = 40.sp, letterSpacing = (-1.02).sp),
    headlineLarge  = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 31.sp, lineHeight = 38.sp, letterSpacing = (-0.62).sp), // H1
    headlineMedium = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 27.sp, lineHeight = 34.sp, letterSpacing = (-0.54).sp), // H2
    headlineSmall  = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 23.sp, lineHeight = 29.sp, letterSpacing = (-0.46).sp), // H3
    titleLarge     = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, lineHeight = 21.sp), // H4 / section header
    titleMedium    = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    titleSmall     = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp, lineHeight = 18.sp),
    bodyLarge      = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.Normal, fontSize = 13.5.sp, lineHeight = 19.sp), // Body
    bodyMedium     = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.Normal, fontSize = 13.5.sp, lineHeight = 19.sp),
    bodySmall      = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp), // Body S
    labelLarge     = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, letterSpacing = 0.sp), // Chip
    labelMedium    = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, letterSpacing = 0.6.sp), // Label/overline
    labelSmall     = TextStyle(fontFamily = Archivo, fontWeight = FontWeight.ExtraBold, fontSize = 9.5.sp, letterSpacing = 0.19.sp) // Nav
)
