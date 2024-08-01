package com.madalin.notelo.core.data.local.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.madalin.notelo.core.data.local.entity.CategoryEntity
import com.madalin.notelo.core.data.local.entity.NoteEntity
import com.madalin.notelo.core.data.local.entity.NoteTagCrossRefEntity
import com.madalin.notelo.core.data.local.entity.TagEntity

data class NoteWithCategoryAndTags(
    @Embedded val note: NoteEntity,

    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: CategoryEntity?,

    @Relation(
        parentColumn = "id", // this (A)
        entityColumn = "id", // this (B)
        associateBy = Junction(
            value = NoteTagCrossRefEntity::class,
            parentColumn = "noteId", // with this (A)
            entityColumn = "tagId" // with this (B)
        )
    )
    val tags: List<TagEntity>
)