package com.madalin.notelo.model

import android.os.Parcelable
import com.google.firebase.firestore.ServerTimestamp
import com.madalin.notelo.ApplicationClass
import com.madalin.notelo.R
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Tag(
    var id: String? = null,
    var categoryId: String? = null,
    var name: String? = null,
    var color: String? = null,
    @ServerTimestamp var createdAt: Date? = null,
    @ServerTimestamp var updatedAt: Date? = null
) : Parcelable {
    companion object {
        val ID_ALL_NOTES: String? = null // used to set the ID of the tag that contains every note
        val NAME_ALL_NOTES = ApplicationClass.context.getString(R.string.all_notes) // used to set the name of the tag that contains every note
    }
}