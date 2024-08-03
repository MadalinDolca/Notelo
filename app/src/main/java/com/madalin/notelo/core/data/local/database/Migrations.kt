package com.madalin.notelo.core.data.local.database

import androidx.room.migration.Migration

val MIGRATION_1_2 = Migration(1, 2) { database ->
    database.execSQL("ALTER TABLE notes ADD COLUMN categoryId TEXT")
}