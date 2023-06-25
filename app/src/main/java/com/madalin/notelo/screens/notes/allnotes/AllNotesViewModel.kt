package com.madalin.notelo.screens.notes.allnotes

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.madalin.notelo.models.Note
import com.madalin.notelo.user.UserData
import com.madalin.notelo.util.DBCollection

class AllNotesViewModel : ViewModel() {
    private val firestore = Firebase.firestore
    private val notesList = mutableListOf<Note>() // list to store the user's notes

    // data holders to observe
    val notesListLiveData by lazy { MutableLiveData<MutableList<Note>>() }
    val errorMessageLiveData by lazy { MutableLiveData<String>() }

    fun getNotesFromFirestore() {
        firestore.collection(DBCollection.NOTES)
            .whereEqualTo("userId", UserData.id)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    errorMessageLiveData.value = error.message
                    return@addSnapshotListener
                }

                notesList.clear()

                for (snapshot in snapshots!!) {
                    val note = snapshot.toObject<Note>()
                    note.id = snapshot.id

                    notesList.add(note)
                }

                notesListLiveData.value = notesList // sets the value and dispatches it to the active observers
            }
    }

    fun findNotes(query: String): List<Note> {
        val foundNotes = mutableListOf<Note>()

        if (query.isEmpty()) {
            return notesList
        } else {
            for (note in notesList) {
                if (note.title?.contains(query) == true) {
                    foundNotes.add(note)
                } else if (note.content?.contains(query) == true) {
                    foundNotes.add(note)
                }
            }
        }

        return foundNotes
    }
}