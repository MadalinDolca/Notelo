package com.madalin.notelo.core.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.madalin.notelo.core.data.local.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Upsert
    suspend fun upsertTag(tag: TagEntity)

    @Update
    suspend fun updateTag(tag: TagEntity)

    @Delete
    suspend fun deleteTag(tag: TagEntity)

    @Query("SELECT * FROM tags WHERE categoryId = :categoryId")
    suspend fun getTagsByCategoryId(categoryId: String): List<TagEntity>

    @Query("SELECT * FROM tags WHERE categoryId = :categoryId")
    fun getTagsByCategoryIdObserver(categoryId: String): Flow<List<TagEntity>>

    @Query("DELETE FROM tags WHERE categoryId = :categoryId")
    suspend fun deleteCategoryTags(categoryId: String)
}