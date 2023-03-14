package com.madalin.notelo.models

import android.os.Parcelable
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Note(
    var id: String = "",
    var userId: String = "",
    var categoryId: String = "",
    var title: String = "",
    var content: String = "",
    var color: String = "",
    var tags: MutableList<String> = mutableListOf(), // tag IDs
    var tagsData: MutableList<Tag> = mutableListOf(), // complete tag data
    var visible: Boolean = PRIVATE,
    @ServerTimestamp var createdAt: Date? = null,
    @ServerTimestamp var updatedAt: Date? = null
) : Parcelable {
    companion object {
        const val PRIVATE = false
        const val PUBLIC = true
    }
}