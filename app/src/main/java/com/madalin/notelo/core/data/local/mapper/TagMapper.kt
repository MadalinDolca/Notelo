package com.madalin.notelo.core.data.local.mapper

import com.madalin.notelo.core.data.local.entity.TagEntity
import com.madalin.notelo.core.domain.model.Tag

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