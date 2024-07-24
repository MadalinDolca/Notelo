package com.madalin.notelo.core.domain.util

import android.content.Context
import androidx.annotation.StringRes

/**
 * Represents different types of strings, including dynamically provided strings,
 * empty strings, and strings resources identified by a resource ID.
 */
sealed class StringValue {
    /**
     * Represents a dynamically provided string value.
     * @property value The value of the dynamic string.
     */
    data class DynamicString(val value: String) : StringValue()

    /**
     * Represents an empty string value.
     */
    object Empty : StringValue()

    /**
     * Represents a string resource identified by a resource ID and optional arguments.
     * @property resId The resource ID of the string resource.
     * @property args Optional arguments for string formatting.
     */
    class StringResource(
        @StringRes val resId: Int,
        vararg val args: Any
    ) : StringValue()

    /**
     * Converts the [StringValue] to a standard [String] representation.
     *
     * @param context The context used for retrieving string resources. Can be null.
     * @return The corresponding [String] representation based on the type of [StringValue].
     */
    fun asString(context: Context?): String {
        return when (this) {
            is Empty -> ""
            is DynamicString -> value
            is StringResource -> context?.getString(resId, *args).orEmpty()
        }
    }
}
