package com.madalin.notelo.core.domain.model

import android.os.Parcelable
import com.madalin.notelo.R
import com.madalin.notelo.core.domain.NoteloApplication
import com.madalin.notelo.core.domain.util.generateId
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Category(
    var id: String = generateId(),
    var name: String,
    var color: String? = null,
    var createdAt: Date = Date(),
    var updatedAt: Date? = null
) : Parcelable {
    companion object {
        /**
         * The ID of the category that contains the uncategorized notes.
         */
        const val ID_UNCATEGORIZED: String = "uncategorized"

        /**
         * The name of the category that contains the uncategorized notes.
         */
        val NAME_UNCATEGORIZED = NoteloApplication.context.getString(R.string.uncategorized)

        /**
         * Returns a [Category] for "uncategorized" notes.
         */
        fun subUncategorized() = Category(
            id = ID_UNCATEGORIZED,
            name = NAME_UNCATEGORIZED
        )

        /**
         * Returns `true` if the given [id] is null or equal to [ID_UNCATEGORIZED], otherwise returns `false`.
         */
        fun isUncategorized(id: String?) = id == ID_UNCATEGORIZED || id == null

    }
}