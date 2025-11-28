package org.mikhailzhdanov.deskbox

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private val primaryLight = Color(0xFF0C6780)
private val onPrimaryLight = Color(0xFFFFFFFF)
private val primaryContainerLight = Color(0xFFBAEAFF)
private val onPrimaryContainerLight = Color(0xFF004D62)
private val secondaryLight = Color(0xFF4C626B)
private val onSecondaryLight = Color(0xFFFFFFFF)
private val secondaryContainerLight = Color(0xFFCFE6F1)
private val onSecondaryContainerLight = Color(0xFF354A53)
private val tertiaryLight = Color(0xFF5B5B7E)
private val onTertiaryLight = Color(0xFFFFFFFF)
private val tertiaryContainerLight = Color(0xFFE2DFFF)
private val onTertiaryContainerLight = Color(0xFF444465)
private val errorLight = Color(0xFFBA1A1A)
private val onErrorLight = Color(0xFFFFFFFF)
private val errorContainerLight = Color(0xFFFFDAD6)
private val onErrorContainerLight = Color(0xFF93000A)
private val backgroundLight = Color(0xFFF5FAFD)
private val onBackgroundLight = Color(0xFF171C1F)
private val surfaceLight = Color(0xFFF5FAFD)
private val onSurfaceLight = Color(0xFF171C1F)
private val surfaceVariantLight = Color(0xFFDCE4E8)
private val onSurfaceVariantLight = Color(0xFF40484C)
private val outlineLight = Color(0xFF70787D)
private val outlineVariantLight = Color(0xFFC0C8CC)
private val scrimLight = Color(0xFF000000)
private val inverseSurfaceLight = Color(0xFF2C3134)
private val inverseOnSurfaceLight = Color(0xFFEDF1F5)
private val inversePrimaryLight = Color(0xFF89D0ED)
private val surfaceDimLight = Color(0xFFD6DBDE)
private val surfaceBrightLight = Color(0xFFF5FAFD)
private val surfaceContainerLowestLight = Color(0xFFFFFFFF)
private val surfaceContainerLowLight = Color(0xFFF0F4F7)
private val surfaceContainerLight = Color(0xFFEAEEF2)
private val surfaceContainerHighLight = Color(0xFFE4E9EC)
private val surfaceContainerHighestLight = Color(0xFFDEE3E6)

val colorScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)