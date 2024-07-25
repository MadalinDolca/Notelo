package com.madalin.notelo.core.presentation.util

import android.content.Context
import androidx.annotation.StringRes

/**
 * Represents different types of strings, including dynamically provided strings,
 * empty strings, and strings resources identified by a resource ID.
 */
sealed class UiText {
    /**
     * Represents a dynamically provided string [value].
     */
    data class Dynamic(val value: String) : UiText()

    /**
     * Represents a string resource identified by [resId] and optional [arguments][args] for
     * string formatting.
     */
    class Resource(@StringRes val resId: Int, vararg val args: Any) : UiText()

    /**
     * Represents an empty string.
     */
    data object Empty : UiText()

    /**
     * Converts this [UiText] to a standard [String] representation and returns it.
     * @param context The [Context] used for retrieving string resources. If `null` it returns an
     * empty string.
     */
    fun asString(context: Context?): String {
        if (context == null) return ""

        return when (this) {
            is Dynamic -> value
            is Resource -> context.getString(resId, *args)
            Empty -> ""
        }
    }
}