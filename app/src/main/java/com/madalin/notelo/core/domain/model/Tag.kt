package com.madalin.notelo.core.domain.model

import android.os.Parcelable
import com.madalin.notelo.R
import com.madalin.notelo.core.domain.NoteloApplication
import com.madalin.notelo.core.domain.util.generateId
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Tag(
    var id: String = generateId(),
    var categoryId: String,
    var name: String,
    var createdAt: Date = Date(),
    var updatedAt: Date? = null
) : Parcelable {
    companion object {
        /**
         * The ID of the tag that contains every note.
         */
        const val ID_ALL_NOTES: String = "allNotes"

        /**
         * The name of the tag that contains every note.
         */
        val NAME_ALL_NOTES = NoteloApplication.context.getString(R.string.all_notes)

        /**
         * The ID of the tag that contains the untagged notes.
         */
        const val ID_UNTAGGED: String = "untagged"

        /**
         * The name of the tag that contains the untagged notes.
         */
        val NAME_UNTAGGED = NoteloApplication.context.getString(R.string.untagged)

        /**
         * Returns a [Tag] for "all notes" that belong to the given [categoryId].
         */
        fun subAllNotes(categoryId: String) = Tag(
            id = ID_ALL_NOTES,
            categoryId = categoryId,
            name = NAME_ALL_NOTES
        )

        /**
         * Returns a [Tag] for "untagged" notes that belong to the given [categoryId].
         */
        fun subUntagged(categoryId: String) = Tag(
            id = ID_UNTAGGED,
            categoryId = categoryId,
            name = NAME_UNTAGGED
        )
    }
}