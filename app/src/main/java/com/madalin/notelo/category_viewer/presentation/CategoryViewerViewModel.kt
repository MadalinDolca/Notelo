package com.madalin.notelo.category_viewer.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madalin.notelo.R
import com.madalin.notelo.core.domain.model.Category
import com.madalin.notelo.core.domain.model.Note
import com.madalin.notelo.core.domain.model.Tag
import com.madalin.notelo.core.domain.repository.local.LocalContentRepository
import com.madalin.notelo.core.presentation.GlobalDriver
import com.madalin.notelo.core.presentation.components.PopupBanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class CategoryViewerViewModel(
    savedStateHandle: SavedStateHandle,
    private val globalDriver: GlobalDriver,
    private val localRepository: LocalContentRepository
) : ViewModel() {
    val categoryId: String? = savedStateHandle["categoryId"]

    private val _categoryState = MutableLiveData<Category>()
    val categoryState: LiveData<Category> get() = _categoryState

    private val _tagNotesMapState = MutableLiveData<Map<Tag, List<Note>>>()
    val tagNotesMapState: LiveData<Map<Tag, List<Note>>> get() = _tagNotesMapState

    init {
        getData()
    }

    /**
     * Obtains the data and the content of the category that has this [categoryId].
     */
    private fun getData() {
        if (categoryId == null) {
            globalDriver.showPopupBanner(
                PopupBanner.TYPE_FAILURE,
                R.string.could_not_get_this_category_content_because_the_category_id_is_null
            )
            return
        }

        getCategoryByIdObserver(categoryId)
        getNotesMappedByTagsObserver(categoryId)
    }

    /**
     * Obtains the category with the given [categoryId] and observes for changes.
     */
    private fun getCategoryByIdObserver(categoryId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            localRepository.getCategoryByIdObserver(categoryId)
                .catch {
                    globalDriver.showPopupBanner(
                        PopupBanner.TYPE_FAILURE,
                        it.message ?: R.string.could_not_get_this_category_content
                    )
                }
                .collect {
                    _categoryState.postValue(it)
                }
        }
    }

    /**
     * Obtains the notes of this [categoryId] mapped by its tags and observes for changes.
     */
    private fun getNotesMappedByTagsObserver(categoryId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            localRepository.getNotesInCategoryMappedByTagsObserver(categoryId)
                .catch {
                    globalDriver.showPopupBanner(
                        PopupBanner.TYPE_FAILURE,
                        it.message ?: R.string.could_not_get_this_category_content
                    )
                }
                .collect {
                    val data = mutableMapOf<Tag, List<Note>>()
                    val allTag = Tag.subAllNotes(categoryId)
                    val allNotes = it.values.flatten().toSet().toList() // unique notes

                    data.put(allTag, allNotes)
                    data.putAll(it)
                    _tagNotesMapState.postValue(data)
                }
        }
    }
}