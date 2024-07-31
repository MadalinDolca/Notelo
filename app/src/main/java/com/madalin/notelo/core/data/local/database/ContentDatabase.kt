package com.madalin.notelo.core.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.madalin.notelo.core.data.local.converter.DateConverter
import com.madalin.notelo.core.data.local.dao.CategoryDao
import com.madalin.notelo.core.data.local.dao.NoteDao
import com.madalin.notelo.core.data.local.dao.TagDao
import com.madalin.notelo.core.data.local.dao.NoteTagDao
import com.madalin.notelo.core.data.local.entity.CategoryEntity
import com.madalin.notelo.core.data.local.entity.NoteEntity
import com.madalin.notelo.core.data.local.entity.NoteTagCrossRefEntity
import com.madalin.notelo.core.data.local.entity.TagEntity

@Database(
    entities = [
        NoteEntity::class, CategoryEntity::class, TagEntity::class,
        NoteTagCrossRefEntity::class
    ],
    version = 1
)
@TypeConverters(DateConverter::class)
abstract class ContentDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun categoryDao(): CategoryDao
    abstract fun tagDao(): TagDao
    abstract fun noteTagDao(): NoteTagDao
}