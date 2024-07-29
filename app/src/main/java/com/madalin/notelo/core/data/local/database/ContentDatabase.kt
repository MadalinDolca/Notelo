package com.madalin.notelo.core.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.madalin.notelo.core.data.local.converter.DateConverter
import com.madalin.notelo.core.data.local.dao.NoteDao
import com.madalin.notelo.core.data.local.entity.NoteEntity

@Database(entities = [NoteEntity::class], version = 1)
@TypeConverters(DateConverter::class)
abstract class ContentDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}