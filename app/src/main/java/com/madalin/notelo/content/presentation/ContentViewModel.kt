package com.madalin.notelo.content.presentation

import androidx.lifecycle.ViewModel
import com.madalin.notelo.content.presentation.categories_list.CategoriesFragment
import com.madalin.notelo.content.presentation.notes_list.NotesFragment

class ContentViewModel : ViewModel() {
    val fragmentsList = listOf(NotesFragment(), CategoriesFragment())
}