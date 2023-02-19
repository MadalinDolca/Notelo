package com.madalin.notelo.collection

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.madalin.notelo.Collection.NOTES

// Hold and manage UI-related data in a life-cycle conscious way
class CollectionViewModel : ViewModel() {
    private val firestore = Firebase.firestore
    private var notesList = mutableListOf<Note>() // for storing the notes

    private val notesListLiveData = MutableLiveData<MutableList<Note>>() // data holder to observe
    private var errorMessage = MutableLiveData<String>()

    // public getters for returning the data holder
    val getNotesListLiveData: LiveData<MutableList<Note>> get() = notesListLiveData
    val getErrorMessage: LiveData<String> get() = errorMessage

    /**
     * Queries the database "notes" collection and adds the data to [notesList].
     * Once every data item has been added, [notesListLiveData]'s value will be set to [notesList].
     */
    fun getNotesFromFirestore() {
        notesList.clear() // clears the current list

        firestore.collection(NOTES).get()
            .addOnSuccessListener { snapshots ->
                for (snapshot in snapshots) {
                    val note = snapshot.toObject<Note>()
                    note.key = snapshot.id
                    notesList.add(note)

                    Log.e("Notes", note.toString())
                }

                notesListLiveData.value = notesList // notesListLiveData.postValue(notesList)
            }
            .addOnFailureListener {
                errorMessage.value = it.message.toString()
            }
    }
}