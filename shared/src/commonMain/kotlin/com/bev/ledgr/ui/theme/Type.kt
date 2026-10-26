package com.bev.ledgr.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import ledgr.shared.generated.resources.Res
import ledgr.shared.generated.resources.manrope_bold
import ledgr.shared.generated.resources.manrope_medium
import ledgr.shared.generated.resources.manrope_regular
import ledgr.shared.generated.resources.manrope_semiBold
import org.jetbrains.compose.resources.Font

@Composable
fun manropeFamilyFont() = FontFamily(
    Font(Res.font.manrope_regular, FontWeight.Normal),
    Font(Res.font.manrope_medium, FontWeight.Medium),
    Font(Res.font.manrope_semiBold, FontWeight.SemiBold),
    Font(Res.font.manrope_bold, FontWeight.Bold)
)

@Composable
fun ledgrTypography(): Typography {
    val manrope = manropeFamilyFont()
    val base = Typography()
    return Typography(
        displayLarge = base.displayLarge.copy(fontFamily = manrope),
        displayMedium = base.displayMedium.copy(fontFamily = manrope),
        displaySmall = base.displaySmall.copy(fontFamily = manrope),
        headlineLarge = base.headlineLarge.copy(fontFamily = manrope),
        headlineMedium = base.headlineMedium.copy(fontFamily = manrope),
        headlineSmall = base.headlineSmall.copy(fontFamily = manrope),
        titleLarge = base.titleLarge.copy(fontFamily = manrope),
        titleMedium = base.titleMedium.copy(fontFamily = manrope),
        titleSmall = base.titleSmall.copy(fontFamily = manrope),
        bodyLarge = base.bodyLarge.copy(fontFamily = manrope),
        bodyMedium = base.bodyMedium.copy(fontFamily = manrope),
        bodySmall = base.bodySmall.copy(fontFamily = manrope),
        labelLarge = base.labelLarge.copy(fontFamily = manrope),
        labelMedium = base.labelMedium.copy(fontFamily = manrope),
        labelSmall = base.labelSmall.copy(fontFamily = manrope)
    )
}
