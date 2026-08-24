package com.liuflow.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * MD3 color tokens for the 4 themes × {light, dark} defined in PRD §10.2.
 * The names follow the Material 3 token convention: <role>Container / on<role> etc.
 */
object Brand {
    val Muted = Color(0xFF79747E)
    val OnMuted = Color(0xFFFFFFFF)
}

data class FlowColorScheme(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiary: Color,
    val onTertiary: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,
    val surface: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val surfaceContainerLow: Color,
    val outline: Color,
    val outlineVariant: Color,
    val error: Color,
    val onError: Color,
    val background: Color,
    val onBackground: Color,
)

private val ClassicLight = FlowColorScheme(
    primary = Color(0xFF6750A4),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF625B71),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Color(0xFF7D5260),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD8E4),
    onTertiaryContainer = Color(0xFF31111D),
    surface = Color(0xFFFEF7FF),
    onSurface = Color(0xFF1C1B1F),
    onSurfaceVariant = Color(0xFF49454F),
    surfaceContainer = Color(0xFFF3EDF7),
    surfaceContainerHigh = Color(0xFFECE6F0),
    surfaceContainerLow = Color(0xFFF7F2FA),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    background = Color(0xFFFEF7FF),
    onBackground = Color(0xFF1C1B1F),
)

private val ClassicDark = FlowColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFFCCC2DC),
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = Color(0xFFEFB8C8),
    onTertiary = Color(0xFF492532),
    tertiaryContainer = Color(0xFF633B48),
    onTertiaryContainer = Color(0xFFFFD8E4),
    surface = Color(0xFF141218),
    onSurface = Color(0xFFE6E1E5),
    onSurfaceVariant = Color(0xFFCAC4D0),
    surfaceContainer = Color(0xFF211F26),
    surfaceContainerHigh = Color(0xFF2B2930),
    surfaceContainerLow = Color(0xFF1D1B20),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    background = Color(0xFF141218),
    onBackground = Color(0xFFE6E1E5),
)

private val NightLight = FlowColorScheme(
    primary = Color(0xFF4A6FA5),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD3E3FD),
    onPrimaryContainer = Color(0xFF001D36),
    secondary = Color(0xFF535F70),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD7E3F8),
    onSecondaryContainer = Color(0xFF101C2B),
    tertiary = Color(0xFF6B5778),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF2DAFF),
    onTertiaryContainer = Color(0xFF251431),
    surface = Color(0xFFFBFCFF),
    onSurface = Color(0xFF1A1C1E),
    onSurfaceVariant = Color(0xFF43474E),
    surfaceContainer = Color(0xFFEFF1F4),
    surfaceContainerHigh = Color(0xFFE9EBED),
    surfaceContainerLow = Color(0xFFF5F6F8),
    outline = Color(0xFF73777F),
    outlineVariant = Color(0xFFC3C7CF),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    background = Color(0xFFFBFCFF),
    onBackground = Color(0xFF1A1C1E),
)

private val NightDark = FlowColorScheme(
    primary = Color(0xFFA6C8FF),
    onPrimary = Color(0xFF003259),
    primaryContainer = Color(0xFF2E4A78),
    onPrimaryContainer = Color(0xFFD3E3FD),
    secondary = Color(0xFFBBC7DB),
    onSecondary = Color(0xFF253140),
    secondaryContainer = Color(0xFF3B4858),
    onSecondaryContainer = Color(0xFFD7E3F8),
    tertiary = Color(0xFFD6BEE4),
    onTertiary = Color(0xFF3B2948),
    tertiaryContainer = Color(0xFF523F5F),
    onTertiaryContainer = Color(0xFFF2DAFF),
    surface = Color(0xFF0F1419),
    onSurface = Color(0xFFE3E3E3),
    onSurfaceVariant = Color(0xFFC3C7CF),
    surfaceContainer = Color(0xFF1B2024),
    surfaceContainerHigh = Color(0xFF252A2E),
    surfaceContainerLow = Color(0xFF161A1E),
    outline = Color(0xFF8D9199),
    outlineVariant = Color(0xFF43474E),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    background = Color(0xFF0F1419),
    onBackground = Color(0xFFE3E3E3),
)

private val ForestLight = FlowColorScheme(
    primary = Color(0xFF2D6A4F),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFA8D5BA),
    onPrimaryContainer = Color(0xFF002111),
    secondary = Color(0xFF526350),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD5E8D0),
    onSecondaryContainer = Color(0xFF101F10),
    tertiary = Color(0xFF38656A),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFBCEBF0),
    onTertiaryContainer = Color(0xFF002023),
    surface = Color(0xFFF5F1E8),
    onSurface = Color(0xFF1B2D1F),
    onSurfaceVariant = Color(0xFF424940),
    surfaceContainer = Color(0xFFEAEFE3),
    surfaceContainerHigh = Color(0xFFE3E9DC),
    surfaceContainerLow = Color(0xFFF0F2EA),
    outline = Color(0xFF727970),
    outlineVariant = Color(0xFFC2C9BD),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    background = Color(0xFFF5F1E8),
    onBackground = Color(0xFF1B2D1F),
)

private val ForestDark = FlowColorScheme(
    primary = Color(0xFF8DD5A0),
    onPrimary = Color(0xFF003920),
    primaryContainer = Color(0xFF0E5238),
    onPrimaryContainer = Color(0xFFA8D5BA),
    secondary = Color(0xFFB9CCB4),
    onSecondary = Color(0xFF253423),
    secondaryContainer = Color(0xFF3B4B38),
    onSecondaryContainer = Color(0xFFD5E8D0),
    tertiary = Color(0xFFA0CFD4),
    onTertiary = Color(0xFF00373B),
    tertiaryContainer = Color(0xFF1E4D52),
    onTertiaryContainer = Color(0xFFBCEBF0),
    surface = Color(0xFF131812),
    onSurface = Color(0xFFE1E4DC),
    onSurfaceVariant = Color(0xFFC2C9BD),
    surfaceContainer = Color(0xFF1B211B),
    surfaceContainerHigh = Color(0xFF262B26),
    surfaceContainerLow = Color(0xFF161A16),
    outline = Color(0xFF8C9387),
    outlineVariant = Color(0xFF424940),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    background = Color(0xFF131812),
    onBackground = Color(0xFFE1E4DC),
)

private val TwilightLight = FlowColorScheme(
    primary = Color(0xFF7A4E7C),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE8C5E5),
    onPrimaryContainer = Color(0xFF2F0F30),
    secondary = Color(0xFF655A6F),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFECDCF1),
    onSecondaryContainer = Color(0xFF201828),
    tertiary = Color(0xFF815150),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD9D9),
    onTertiaryContainer = Color(0xFF331112),
    surface = Color(0xFFF0EBF0),
    onSurface = Color(0xFF2A1A2A),
    onSurfaceVariant = Color(0xFF4E444E),
    surfaceContainer = Color(0xFFE5DFE5),
    surfaceContainerHigh = Color(0xFFDFD9DF),
    surfaceContainerLow = Color(0xFFEBE5EB),
    outline = Color(0xFF7F747E),
    outlineVariant = Color(0xFFCFC2CE),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    background = Color(0xFFF0EBF0),
    onBackground = Color(0xFF2A1A2A),
)

private val TwilightDark = FlowColorScheme(
    primary = Color(0xFFEFB0EE),
    onPrimary = Color(0xFF48194B),
    primaryContainer = Color(0xFF5F3362),
    onPrimaryContainer = Color(0xFFE8C5E5),
    secondary = Color(0xFFCFBAD4),
    onSecondary = Color(0xFF362D3D),
    secondaryContainer = Color(0xFF4D4355),
    onSecondaryContainer = Color(0xFFECDCF1),
    tertiary = Color(0xFFEBB7B7),
    onTertiary = Color(0xFF4A2225),
    tertiaryContainer = Color(0xFF653A3A),
    onTertiaryContainer = Color(0xFFFFD9D9),
    surface = Color(0xFF161217),
    onSurface = Color(0xFFEBDFEB),
    onSurfaceVariant = Color(0xFFCFC2CE),
    surfaceContainer = Color(0xFF1E1A1F),
    surfaceContainerHigh = Color(0xFF28242C),
    surfaceContainerLow = Color(0xFF1A161B),
    outline = Color(0xFF988C99),
    outlineVariant = Color(0xFF4E444E),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    background = Color(0xFF161217),
    onBackground = Color(0xFFEBDFEB),
)

/** Maps a [com.liuflow.app.data.model.FlowTheme] + dark/light to a [FlowColorScheme]. */
fun flowColorScheme(themeId: String, dark: Boolean): FlowColorScheme {
    val light = when (themeId) {
        "night" -> NightLight
        "forest" -> ForestLight
        "twilight" -> TwilightLight
        else -> ClassicLight
    }
    val darkScheme = when (themeId) {
        "night" -> NightDark
        "forest" -> ForestDark
        "twilight" -> TwilightDark
        else -> ClassicDark
    }
    return if (dark) darkScheme else light
}
