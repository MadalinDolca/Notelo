package com.madalin.notelo.screens.notes.categorynotes

import androidx.lifecycle.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.madalin.notelo.ApplicationClass
import com.madalin.notelo.R
import com.madalin.notelo.models.Note
import com.madalin.notelo.models.Tag
import com.madalin.notelo.util.DBCollection

// Store and manage UI-related data in a lifecycle-conscious way
class CategoryNotesViewModel : ViewModel() {
    private val firestore = Firebase.firestore

    // lists to store category related data
    var categoryNotesList = mutableListOf<Note>()
    var categoryTagsList = mutableListOf<Tag>()

    // LiveData holders to observe
    val notesListLiveData by lazy { MutableLiveData<MutableList<Note>>() } // initialized only once and only when it's called
    val tagsListLiveData by lazy { MutableLiveData<MutableList<Tag>>() }
    val errorMessageLiveData by lazy { MutableLiveData<String>() }

    /**
     * [LiveData] that observes changes in the [tagsListLiveData] and maps it's value to a `Map<Tag, List<Note>>`
     * data structure that groups the notes by tag using `Transformations.map()`.
     */
    val notesByTagLiveData = tagsListLiveData.map { tags -> //Transformations.map(tagsListLiveData) { tags ->
        val notesByTag = mutableMapOf<Tag, List<Note>>() //mutableListOf<Pair<Tag, List<Note>>>()

        // adds every note from the category at the beginning of the map
        val allNotesTag = Tag(name = ApplicationClass.context.getString(R.string.all_notes))
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

        notesByTag // assigns this value to this LiveData
    }

    /**
     * Queries the database to find the notes associated with the given [categoryId] and adds them to [categoryNotesList].
     * Once every data item has been added the value of [notesListLiveData] is set to [categoryNotesList].
     */
    fun getNotesByCategoryFromFirestore(categoryId: String) {
        categoryNotesList.clear() // clears the current notes list

        firestore.collection(DBCollection.NOTES)
            .whereEqualTo("categoryId", categoryId)
            .get()
            .addOnSuccessListener { snapshots ->
                for (snapshot in snapshots) {
                    val note = snapshot.toObject<Note>()
                    note.id = snapshot.id
                    categoryNotesList.add(note)
                }

                notesListLiveData.value = categoryNotesList // sets the value and dispatches it to the active observers //notesListLiveData.postValue(notesList)
            }
            .addOnFailureListener {
                errorMessageLiveData.value = it.message
            }
    }

    /**
     * Queries the database to find the tags associated with the given [categoryId] and adds them to [categoryTagsList].
     * Once every tag has been added the value of [tagsListLiveData] is set to [categoryTagsList].
     */
    fun getTagsByCategoryFromFirestore(categoryId: String) {
        categoryTagsList.clear() // clears the current tags list

        firestore.collection(DBCollection.TAGS)
            .whereEqualTo("categoryId", categoryId)
            .get()
            .addOnSuccessListener { snapshots ->
                for (snapshot in snapshots) {
                    val tag = snapshot.toObject<Tag>()
                    tag.id = snapshot.id
                    categoryTagsList.add(tag)
                }

                tagsListLiveData.value = categoryTagsList // sets the value and dispatches it to the active observers
            }
            .addOnFailureListener {
                errorMessageLiveData.value = it.message
            }
    }
}