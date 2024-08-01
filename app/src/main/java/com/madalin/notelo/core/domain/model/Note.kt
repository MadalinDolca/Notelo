package com.madalin.notelo.core.domain.model

import android.os.Parcelable
import com.madalin.notelo.core.domain.util.generateId
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Note(
    var id: String = generateId(),
    var userId: String? = null,
    var categoryId: String? = null,
    var title: String,
    var content: String,
    var public: Boolean = false,
    var createdAt: Date = Date(),
    var updatedAt: Date? = null
) : Parcelable {
    var category: Category? = null // category info (optional)
    var tags: List<Tag> = emptyList() // tags info (optional)
}