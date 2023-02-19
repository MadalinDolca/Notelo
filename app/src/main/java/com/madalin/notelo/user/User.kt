package com.madalin.notelo.user

import com.google.firebase.firestore.ServerTimestamp
import java.io.Serializable
import java.util.*

data class User(
    var key: String = "",
    var email: String = "",
    var role: String = ROLE_DEFAULT
) : Serializable {

    var name: String = ""

    @ServerTimestamp
    private var createdAt: Date = Date() // private to avoid JVM clash

    @ServerTimestamp
    private var updatedAt: Date = Date()

    companion object {
        const val ROLE_ADMIN = "admin"
        const val ROLE_DEFAULT = "default"
    }

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