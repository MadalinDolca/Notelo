package com.madalin.notelo.core.presentation.util

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

object ThemeState {
    private const val THEME_PREFERENCES = "theme_preferences"
    private const val IS_DARK_MODE_ENABLED = "is_dark_mode_enabled"

    /**
     * Applies the stored theme state in [SharedPreferences] to the application.
     * @param context Context used to access the [SharedPreferences].
     */
    fun setModeFromPreferences(context: Context) {
        val sharedPreferences = context.getSharedPreferences(THEME_PREFERENCES, Context.MODE_PRIVATE)
        val isDarkModeEnabled = sharedPreferences.getBoolean(IS_DARK_MODE_ENABLED, false)

        AppCompatDelegate.setDefaultNightMode(
            if (isDarkModeEnabled) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    /**
     * Switches the theme mode between light and dark and stores the new state in [SharedPreferences].
     * @param context Context used to access the [SharedPreferences].
     */
    fun switchMode(context: Context) {
        val currentMode = AppCompatDelegate.getDefaultNightMode()
        if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            storeState(context, false)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            storeState(context, true)
        }
    }

    /**
     * Stores the given [isDarkMode] theme state in [SharedPreferences].
     * @param context Context used to access the [SharedPreferences].
     */
    private fun storeState(context: Context, isDarkMode: Boolean) {
        val sharedPreferences = context.getSharedPreferences(THEME_PREFERENCES, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putBoolean(IS_DARK_MODE_ENABLED, isDarkMode)
            apply()
        }
    }
}