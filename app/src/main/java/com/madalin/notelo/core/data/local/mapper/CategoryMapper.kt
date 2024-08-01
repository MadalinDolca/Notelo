package com.madalin.notelo.core.data.local.mapper

import com.madalin.notelo.core.data.local.entity.CategoryEntity
import com.madalin.notelo.core.domain.model.Category

/**
 * Returns a [Category] representation of this [CategoryEntity].
 */
fun CategoryEntity.toCategoryDomainModel() = Category(
    id = id,
    name = name,
    color = color,
    createdAt = createdAt,
    updatedAt = updatedAt
)

/**
 * Returns a [CategoryEntity] representation of this [Category].
 */
fun Category.toCategoryEntity() = CategoryEntity(
    id = id,
    name = name,
    color = color,
    createdAt = createdAt,
    updatedAt = updatedAt
)