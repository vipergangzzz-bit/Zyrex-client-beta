package com.zyrex.utils.render

import com.zyrex.utils.render.ColorUtils.withAlpha
import java.awt.Color

class ColorSettingsFloat(owner: Any, name: String, val index: Int? = null, generalApply: () -> Boolean = { true }) {
    private val colors = Color(
        if ((index ?: 0) % 3 == 1) 255 else 0,
        if ((index ?: 0) % 3 == 2) 255 else 0,
        if ((index ?: 0) % 3 == 0) 255 else 0
    )

    val color: Color get() = colors
}

class ColorSettingsInteger(
    owner: Any, name: String? = null,
    val index: Int? = null,
    applyMax: Boolean = false,
    generalApply: () -> Boolean = { true }
) {
    private val max = if (applyMax) 255 else 0
    private var color: Color = Color(max, max, max, 255)

    fun color(a: Int = color.alpha) = color().withAlpha(a)

    fun color() = color

    fun with(r: Int = color().red, g: Int = color().green, b: Int = color().blue, a: Int = color().alpha): ColorSettingsInteger {
        color = Color(r, g, b, a)
        return this
    }

    fun with(color: Color) = with(color.red, color.green, color.blue, color.alpha)
}

fun List<ColorSettingsFloat>.toColorArray(max: Int) = (0 until max).map {
    val colors = this[it].color
    floatArrayOf(
        colors.red.toFloat() / 255f,
        colors.green.toFloat() / 255f,
        colors.blue.toFloat() / 255f,
        1f
    )
}
