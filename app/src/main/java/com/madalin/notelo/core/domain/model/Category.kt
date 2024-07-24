package com.madalin.notelo.core.domain.model

import android.os.Parcelable
import com.google.firebase.firestore.ServerTimestamp
import com.madalin.notelo.core.domain.NoteloApplication
import com.madalin.notelo.R
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Category(
    var id: String? = null,
    var userId: String? = null,
    var name: String? = null,
    var color: String? = null,
    var visible: Boolean = VISIBLE_PRIVATE,
    @ServerTimestamp var createdAt: Date? = null,
    @ServerTimestamp var updatedAt: Date? = null
) : Parcelable {
    companion object {
        val ID_UNCATEGORIZED: String? = null // used as an ID for the category that contains the uncategorized notes
        val NAME_UNCATEGORIZED = NoteloApplication.context.getString(R.string.uncategorized) // used as the name for the category that contains the uncategorized notes
        const val VISIBLE_PRIVATE = false // used to set the visibility of the category as private
        const val VISIBLE_PUBLIC = true // used to set the visibility of the category as public
    }
}