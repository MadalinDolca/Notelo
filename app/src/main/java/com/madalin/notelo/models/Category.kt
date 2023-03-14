package com.madalin.notelo.models

import android.os.Parcelable
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import java.io.Serializable
import java.util.*

@Parcelize
data class Category(
    var id: String = "",
    var userId: String = "",
    var name: String = "",
    var color: String = "",
    @ServerTimestamp var createdAt: Date? = null,
    @ServerTimestamp var updatedAt: Date? = null
) : Parcelable {
    companion object {
        const val CATEGORY_ID_UNCATEGORIZED = "CATEGORY_ID_UNCATEGORIZED"
    }
}