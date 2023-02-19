package com.madalin.notelo.utilities

import android.content.ContentResolver
import android.graphics.Color
import android.os.Build
import android.provider.Settings
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding

object EdgeToEdge {
    //enum class Inset { BARS, GESTURES }
    //enum class Spacing { MARGIN, PADDING }
    //enum class Direction { LEFT, TOP, RIGHT, BOTTOM }

    const val SPACING_MARGIN = 13000
    const val SPACING_PADDING = 13001

    const val DIRECTION_LEFT = 14000
    const val DIRECTION_TOP = 14001
    const val DIRECTION_RIGHT = 14002
    const val DIRECTION_BOTTOM = 14003

    /**
     * Expands the given activity on the entire screen and makes the system bars transparent.
     * @param givenActivity the `Activity` to expand
     */
    private fun fullscreen(givenActivity: AppCompatActivity) {
        WindowCompat.setDecorFitsSystemWindows(givenActivity.window, false) // full screen
        givenActivity.window.statusBarColor = Color.TRANSPARENT // transparent status bar
        givenActivity.window.navigationBarColor = Color.TRANSPARENT // transparent navigation bar
    }

    /**
     * Detects if the gesture navigation is enabled.
     * @return `true` if the gesture navigation is enabled, `false` otherwise
     */
    private fun isGestureNavigationEnabled(contentResolver: ContentResolver?): Boolean {
        return Settings.Secure.getInt(contentResolver, "navigation_mode", 0) == 2
    }

    /**
     * Applies spaces to the given view using `Insets` in order to remove the overlaps with system bars.
     * @param givenActivity `Activity` from which to obtain its ContentResolver and apply [fullscreen]
     * @param givenView `View` to apply `Insets` to
     * @param spacing the type of [Spacing] to apply on the [givenView]
     * @param direction the view [Direction] to which the spacing should be applied
     */
    fun edgeToEdge(givenActivity: AppCompatActivity, givenView: View, spacing: Int, direction: Int?) {
        // applies Edge to Edge if the OS version >= Q (API 29) and if the Gesture Navigation is enabled
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (isGestureNavigationEnabled(givenActivity.contentResolver)) {
                fullscreen(givenActivity)

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
        }
    }
}