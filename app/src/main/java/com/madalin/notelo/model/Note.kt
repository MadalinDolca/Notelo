package com.madalin.notelo.model

import android.os.Parcelable
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Note(
    var id: String? = null,
    var userId: String? = null,
    var categoryId: String? = null,
    var title: String? = null,
    var content: String? = null,
    var color: String? = null,
    var tags: MutableList<String?> = mutableListOf(), // tag IDs
    var tagsData: MutableList<Tag> = mutableListOf(), // complete tag data
    var visible: Boolean = VISIBLE_PRIVATE,
    @ServerTimestamp var createdAt: Date? = null,
    @ServerTimestamp var updatedAt: Date? = null
) : Parcelable {
    companion object {
        const val VISIBLE_PRIVATE = false // used to set the visibility of the note as private
        const val VISIBLE_PUBLIC = true // used to set the visibility of the note as public
    }
}