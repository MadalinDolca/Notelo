package com.madalin.notelo.feature.notesandcategories.categoryviewer

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.madalin.notelo.component.PopupBanner
import com.madalin.notelo.feature.notesandcategories.categoryviewer.tagnotes.TagNotesFragment
import com.madalin.notelo.model.Category
import com.madalin.notelo.model.Note
import com.madalin.notelo.model.Tag
import com.madalin.notelo.repository.FirebaseContentRepository
import kotlin.collections.set

// Store and manage UI-related data in a lifecycle-conscious way
class CategoryViewerViewModel(
    private val repository: FirebaseContentRepository
) : ViewModel() {
    var category: Category? = null // category from navigation argument

    // lists to store category related data
    var categoryNotesList = mutableListOf<Note>()
    var categoryTagsList = mutableListOf<Tag>()

    // LiveData holders to observe
    private val _notesListLiveData = MutableLiveData<MutableList<Note>>()
    //val notesListLiveData: LiveData<MutableList<Note>> get() = _notesListLiveData

    private val _tagsListLiveData = MutableLiveData<MutableList<Tag>>()
    //val tagsListLiveData: LiveData<MutableList<Tag>> get() = _tagsListLiveData

    private val _notesByTagLiveData = groupNotesByTags()
    val notesByTagLiveData: LiveData<MutableMap<Tag, List<Note>>> get() = _notesByTagLiveData

    private val _popupMessageLiveData = MutableLiveData<Pair<Int, String>>()
    val popupMessageLiveData: LiveData<Pair<Int, String>> get() = _popupMessageLiveData

    /**
     * Queries the database to find the notes associated with the given [categoryId] and adds them to [categoryNotesList].
     * Once every data item has been added the value of [notesListLiveData] is set to [categoryNotesList].
     */
    fun getNotesByCategoryFromFirestore() {
        val categoryId = category?.id ?: return

        repository.getNotesByCategoryIdListener(categoryId,
            onSuccess = {
                categoryNotesList.clear() // clears the current notes list
                categoryNotesList.addAll(it)

                _notesListLiveData.value = categoryNotesList // sets the value and dispatches it to the active observers
            },
            onFailure = {
                it?.let { _popupMessageLiveData.value = Pair(PopupBanner.TYPE_FAILURE, it) }
            })
    }

    /**
     * Queries the database to find the tags associated with the given [categoryId] and adds them to [categoryTagsList].
     * Once every tag has been added the value of [tagsListLiveData] is set to [categoryTagsList].
     */
    fun getTagsByCategoryFromFirestore() {
        val categoryId = category?.id ?: return

        repository.getTagsByCategoryIdListener(categoryId,
            onSuccess = {
                categoryTagsList.clear() // clears the current tags list
                categoryTagsList.addAll(it)

                _tagsListLiveData.value = categoryTagsList
            },
            onFailure = {
                it?.let { _popupMessageLiveData.value = Pair(PopupBanner.TYPE_FAILURE, it) }
            })
    }

    /**
     * [LiveData] that observes changes in the [tagsListLiveData] and maps it's value to a `Map<Tag, List<Note>>`
     * data structure that groups the notes by tag using `Transformations.map()`.
     */
    private fun groupNotesByTags() = _tagsListLiveData.map { tags -> //Transformations.map(tagsListLiveData) { tags ->
        val notesByTag = mutableMapOf<Tag, List<Note>>() //mutableListOf<Pair<Tag, List<Note>>>()

        if (categoryNotesList.isNotEmpty()) {
            // adds every note from the category at the beginning of the map
            val allNotesTag = Tag(id = Tag.ID_ALL_NOTES, name = Tag.NAME_ALL_NOTES)
            notesByTag[allNotesTag] = categoryNotesList

            // adds to each note the complete data of their tags
            categoryNotesList.forEach { note ->
                note.tags.forEach { tag ->
                    categoryTagsList.find { it.id == tag }?.let {// if there is a tag with that name
                        note.tagsData.add(it) // it gets added to the list
                    }
                }
            }

            // iterates over the tags list and creates a new list of notes by filtering notesListLiveData's value according to the tags
            tags.forEach { tag ->
                val filteredNotes = categoryNotesList.filter { note ->
                    note.tags.contains(tag.id)
                }

                // buils the map using the tag as a key and the filtered notes as a list
                notesByTag[tag] = filteredNotes
            }
        }

        // removes the entries that have no notes associated with a tag
        /*for (entry in notesByTag) {
            if (entry.value.isEmpty()) {
                notesByTag.remove(entry.key)
            }
        }*/

        notesByTag // assigns this value to this LiveData
    }
}