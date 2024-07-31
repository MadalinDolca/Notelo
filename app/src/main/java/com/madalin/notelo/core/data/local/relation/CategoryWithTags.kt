package com.madalin.notelo.core.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.madalin.notelo.core.data.local.entity.CategoryEntity
import com.madalin.notelo.core.data.local.entity.TagEntity

data class CategoryWithTags(
    @Embedded val category: CategoryEntity,
    @Relation(
        parentColumn = "id", // category's primary key
        entityColumn = "categoryId" // tag's foreign key for the category
    )
    val tags: List<TagEntity>
)