package com.madalin.notelo.feature.notesandcategories

import androidx.lifecycle.ViewModel
import com.madalin.notelo.feature.notesandcategories.categories.CategoriesFragment
import com.madalin.notelo.feature.notesandcategories.notes.NotesFragment

class NotesAndCategoriesViewModel : ViewModel() {
    val fragmentsList = listOf(NotesFragment(), CategoriesFragment())
}