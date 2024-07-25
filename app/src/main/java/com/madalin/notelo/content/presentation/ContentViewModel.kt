package com.madalin.notelo.content.presentation

import androidx.lifecycle.ViewModel
import com.madalin.notelo.categories_list.presentation.CategoriesFragment
import com.madalin.notelo.notes_list.presentation.NotesFragment

class ContentViewModel : ViewModel() {
    val fragmentsList = listOf(NotesFragment(), CategoriesFragment())
}