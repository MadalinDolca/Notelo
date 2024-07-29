package com.madalin.notelo.core.domain.model

import android.os.Parcelable
import com.google.firebase.firestore.ServerTimestamp
import com.madalin.notelo.core.domain.util.generateId
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Note(
    var id: String = generateId(),
    var userId: String? = null,
    var title: String = "",
    var content: String = "",
    var public: Boolean = false,
    @ServerTimestamp var createdAt: Date? = null,
    @ServerTimestamp var updatedAt: Date? = null
) : Parcelable {
    var tags: List<Tag> = emptyList()
}