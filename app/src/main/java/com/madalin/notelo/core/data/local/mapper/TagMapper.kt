package com.madalin.notelo.core.data.local.mapper

import com.madalin.notelo.core.data.local.entity.TagEntity
import com.madalin.notelo.core.data.local.relation.NullTag
import com.madalin.notelo.core.domain.model.Category
import com.madalin.notelo.core.domain.model.Tag
import java.util.Date

/**
 * Returns a [Tag] representation of this [TagEntity].
 */
fun TagEntity.toTagDomainModel() = Tag(
    id = id,
    categoryId = categoryId,
    name = name,
    createdAt = createdAt,
    updatedAt = updatedAt
)

/**
 * Returns a [TagEntity] representation of this [Tag].
 */
fun Tag.toTagEntity() = TagEntity(
    id = id,
    categoryId = categoryId,
    name = name,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun NullTag.toTagDomainModel() = Tag(
    id = id ?: Tag.ID_UNTAGGED,
    categoryId = categoryId ?: Category.ID_UNCATEGORIZED,
    name = name ?: Tag.NAME_UNTAGGED,
    createdAt = createdAt ?: Date(),
    updatedAt = updatedAt
)