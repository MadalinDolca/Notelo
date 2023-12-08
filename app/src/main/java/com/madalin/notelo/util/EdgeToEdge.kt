package com.madalin.notelo.util

import android.app.Activity
import android.content.ContentResolver
import android.graphics.Color
import android.os.Build
import android.provider.Settings
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.view.WindowCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding

object EdgeToEdge {
    const val SPACING_MARGIN = 13000
    const val SPACING_PADDING = 13001

    const val DIRECTION_LEFT = 14000
    const val DIRECTION_TOP = 14001
    const val DIRECTION_RIGHT = 14002
    const val DIRECTION_BOTTOM = 14003

    /**
     * Makes [givenActivity] fullscreen if the OS version is >= [Build.VERSION_CODES.Q] and if the
     * Gesture Navigation is enabled.
     */
    fun edgeToEdge(givenActivity: Activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q // if the OS version >= Q (API 29)
            || !isGestureNavigationEnabled(givenActivity.contentResolver) // if Gesture Navigation is enabled
        ) return

        fullscreen(givenActivity)
    }

    /**
     * Applies spaces to [givenView] to not overlap with the
     * system bars if the OS version is >= [Build.VERSION_CODES.Q] and Gesture Navigation is enabled.
     * @param spacing the type of [Spacing] to apply on the [givenView]
     * @param direction the view [Direction] to which the spacing should be applied
     */
    fun edgeToEdge(givenActivity: Activity?, givenView: View, spacing: Int, direction: Int? = null) {
        if (givenActivity == null
            || Build.VERSION.SDK_INT < Build.VERSION_CODES.Q // if the OS version >= Q (API 29)
            || !isGestureNavigationEnabled(givenActivity.contentResolver) // if Gesture Navigation is enabled
        ) return

        //fullscreen(givenActivity)

        givenView.setOnApplyWindowInsetsListener { view, windowInsets ->
            // applies Insets as margins for the view
            if (spacing == SPACING_MARGIN) {
                view.updateLayoutParams<MarginLayoutParams> {
                    when (direction) {
                        DIRECTION_LEFT -> leftMargin = windowInsets.systemWindowInsetLeft
                        DIRECTION_TOP -> topMargin = windowInsets.systemWindowInsetTop
                        DIRECTION_RIGHT -> rightMargin = windowInsets.systemWindowInsetRight
                        DIRECTION_BOTTOM -> bottomMargin = windowInsets.systemWindowInsetBottom
                        else -> {}
                    }
                }
            }
            // applies Insets as paddings for the view
            else if (spacing == SPACING_PADDING) {
                when (direction) {
                    DIRECTION_LEFT -> view.updatePadding(windowInsets.systemWindowInsetLeft, view.paddingTop, view.paddingRight, view.paddingBottom)
                    DIRECTION_TOP -> view.updatePadding(view.paddingLeft, windowInsets.systemWindowInsetTop, view.paddingRight, view.paddingBottom)
                    DIRECTION_RIGHT -> view.updatePadding(view.paddingLeft, view.paddingTop, windowInsets.systemWindowInsetRight, view.paddingBottom)
                    DIRECTION_BOTTOM -> view.updatePadding(view.paddingLeft, view.paddingTop, view.paddingRight, windowInsets.systemWindowInsetBottom)
                    else -> {}
                }
            }

            // returns the Insets
            windowInsets
        }
    }

    /**
     * Expands the [givenActivity] on the entire screen and makes the system bars transparent.
     */
    private fun fullscreen(givenActivity: Activity) {
        WindowCompat.setDecorFitsSystemWindows(givenActivity.window, false) // full screen
        givenActivity.window.statusBarColor = Color.TRANSPARENT // transparent status bar
        givenActivity.window.navigationBarColor = Color.TRANSPARENT // transparent navigation bar
    }

    /**
     * Detects if the gesture navigation is enabled for the given [contentResolver].
     * @return `true` if the gesture navigation is enabled, `false` otherwise
     */
    private fun isGestureNavigationEnabled(contentResolver: ContentResolver?): Boolean {
        return Settings.Secure.getInt(contentResolver, "navigation_mode", 0) == 2
    }
}