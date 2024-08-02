package com.madalin.notelo.core.data.local.relation

import java.util.Date

data class NullTag(
    var id: String?,
    var categoryId: String?,
    var name: String?,
    var createdAt: Date?,
    var updatedAt: Date?
)