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
    }
}