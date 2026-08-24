package com.liuflow.app.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.liuflow.app.R
import com.liuflow.app.ui.theme.Brand

/**
 * Task category for a focus session.
 * Kept as an enum to align with the closed set defined in PRD §5A.4.
 */
enum class Category(
    val id: String,
    @androidx.annotation.StringRes val labelRes: Int,
    val icon: ImageVector,
    /** Distinctive accent color per category (used in stats and chips). */
    val accent: Color,
) {
    WRITING("writing", R.string.category_writing, Icons.Filled.Edit, Color(0xFFC73E1D)),
    CODING("coding", R.string.category_coding, Icons.Filled.Code, Color(0xFF4A6FA5)),
    READING("reading", R.string.category_reading, Icons.Filled.MenuBook, Color(0xFFD4A574)),
    STUDYING("studying", R.string.category_studying, Icons.Filled.School, Color(0xFF2D6A4F)),
    DESIGN("design", R.string.category_design, Icons.Filled.Brush, Color(0xFFD89BAE)),
    OTHER("other", R.string.category_other, Icons.Filled.Circle, Brand.Muted);

    companion object {
        fun fromId(id: String?): Category? = entries.firstOrNull { it.id == id }
    }
}
