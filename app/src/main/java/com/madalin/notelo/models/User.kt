package com.madalin.notelo.models

import java.io.Serializable

data class User(
    var name: String = "N/N",
    var email: String,
    var role: String
) : Serializable {

    var key = ""
    var createdAt = ""
    var updatedAt = ""

    companion object {
        const val ADMIN_ROLE = "admin"
        const val DEFAULT_ROLE = "default"
    }
}
