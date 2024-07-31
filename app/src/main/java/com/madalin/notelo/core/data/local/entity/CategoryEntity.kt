package com.madalin.notelo.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "categories")
class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val color: String?,
    val createdAt: Date,
    val updatedAt: Date?
)