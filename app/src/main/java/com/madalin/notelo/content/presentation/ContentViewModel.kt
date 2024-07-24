package com.madalin.notelo.content.presentation

import androidx.lifecycle.ViewModel
import com.madalin.notelo.feature.notesandcategories.categories.CategoriesFragment
import com.madalin.notelo.notes_list.presentation.NotesFragment

class ContentViewModel : ViewModel() {
    val fragmentsList = listOf(NotesFragment(), CategoriesFragment())
}