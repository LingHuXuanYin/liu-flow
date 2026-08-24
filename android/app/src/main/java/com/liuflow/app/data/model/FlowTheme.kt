package com.liuflow.app.data.model

import androidx.annotation.StringRes
import com.liuflow.app.R

/**
 * Top-level theme picker. Dark/light variant is controlled by system.
 * Color tokens are defined in [com.liuflow.app.ui.theme].
 */
enum class FlowTheme(
    val id: String,
    @StringRes val labelRes: Int,
) {
    CLASSIC("classic", R.string.settings_theme_classic),
    NIGHT("night", R.string.settings_theme_night),
    FOREST("forest", R.string.settings_theme_forest),
    TWILIGHT("twilight", R.string.settings_theme_twilight);

    companion object {
        fun fromId(id: String?): FlowTheme = entries.firstOrNull { it.id == id } ?: CLASSIC
    }
}

/** Dark mode override. */
enum class DarkMode(@StringRes val labelRes: Int) {
    SYSTEM(R.string.settings_dark_system),
    LIGHT(R.string.settings_dark_light),
    DARK(R.string.settings_dark_dark);
}
