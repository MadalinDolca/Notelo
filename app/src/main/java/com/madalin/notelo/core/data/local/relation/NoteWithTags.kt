package com.madalin.notelo.core.data.local.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.madalin.notelo.core.data.local.entity.NoteEntity
import com.madalin.notelo.core.data.local.entity.NoteTagCrossRefEntity
import com.madalin.notelo.core.data.local.entity.TagEntity

data class NoteWithTags(
    @Embedded val note: NoteEntity,
    @Relation(
        parentColumn = "id", // note's primary key
        entityColumn = "tagId", // tag's primary key
        associateBy = Junction(NoteTagCrossRefEntity::class)
    )
    val tags: List<TagEntity>
)