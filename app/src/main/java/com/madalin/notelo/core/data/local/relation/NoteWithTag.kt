package com.madalin.notelo.core.data.local.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.madalin.notelo.core.data.local.entity.NoteEntity
import com.madalin.notelo.core.data.local.entity.NoteTagCrossRefEntity
import com.madalin.notelo.core.data.local.entity.TagEntity

data class NoteWithTag(
    @Embedded val note: NoteEntity? = null,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = NoteTagCrossRefEntity::class,
            parentColumn = "noteId",
            entityColumn = "tagId"
        )
    )
    val tag: TagEntity? = null
)