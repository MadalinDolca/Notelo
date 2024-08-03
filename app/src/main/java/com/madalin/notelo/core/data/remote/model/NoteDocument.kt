package com.madalin.notelo.core.data.remote.model

import android.os.Parcelable
import com.google.firebase.firestore.ServerTimestamp
import com.madalin.notelo.core.domain.util.generateId
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class NoteDocument(
    var id: String = generateId(),
    var userId: String? = null,
    var title: String = "",
    var content: String = "",
    var public: Boolean = false,
    var createdAt: Date = Date(),
    @ServerTimestamp var updatedAt: Date? = null
) : Parcelable