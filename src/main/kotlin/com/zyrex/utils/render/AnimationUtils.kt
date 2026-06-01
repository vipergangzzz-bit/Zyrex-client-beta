package com.zyrex.utils.render

import kotlin.math.pow
import kotlin.math.sin

object AnimationUtils {
    fun easeOut(t: Float, d: Float) = (t / d - 1).pow(3) + 1

    fun easeOutElastic(x: Float) =
        when (x) {
            0f, 1f -> x
            else -> 2f.pow(-10 * x) * sin((x * 10 - 0.75f) * (2 * Math.PI / 3f).toFloat()) + 1
        }
}
