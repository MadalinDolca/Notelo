package com.madalin.notelo.collection

import com.google.firebase.firestore.ServerTimestamp
import java.io.Serializable
import java.util.*

data class Note(
    var key: String = "",
    var title: String = "No title",
    var content: String = "No content",
    var color: String = ""
) : Serializable {

    @ServerTimestamp
    private var createdAt: Date = Date() // private to avoid JVM clash

    @ServerTimestamp
    private var updatedAt: Date = Date()

    fun getCreatedAt(): Date {
        return createdAt
    }

    fun setCreatedAt(createdAt: Date) {
        this.createdAt = createdAt
    }

    fun getUpdatedAt(): Date {
        return updatedAt
    }

    fun setUpdatedAt(updatedAt: Date) {
        this.updatedAt = updatedAt
    }
}
