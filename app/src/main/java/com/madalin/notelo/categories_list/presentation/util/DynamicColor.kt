package com.madalin.notelo.categories_list.presentation.util

import android.graphics.Color
import androidx.core.graphics.ColorUtils

object DynamicColor {

    /**
     * Returns a color in contrast with the given [color].
     * If the [color] is **dark** then it returns a **brighter color** with the given [intensity].
     * If the [color] is **bright** then it returns a **darker color** with the given [intensity].
     * @param color given color
     * @param intensity blending radio. Default is `0.7f`
     * @return color in contrast with the given [color]
     */
    fun getDynamicColor(color: Int, intensity: Float = 0.7f): Int {
        val luminance = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255 // calculates the luminance

        return if (luminance in 0.5..0.8) // if is dark
            ColorUtils.blendARGB(color, Color.BLACK, intensity)
        else if (luminance > 0.8) // if is black
            ColorUtils.blendARGB(color, Color.WHITE, intensity)
        else // if is white
            ColorUtils.blendARGB(color, Color.BLACK, intensity)
    }
}