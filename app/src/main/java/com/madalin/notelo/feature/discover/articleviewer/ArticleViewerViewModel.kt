package com.madalin.notelo.feature.discover.articleviewer

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.madalin.notelo.api.Article
import com.madalin.notelo.model.Note
import com.madalin.notelo.user.UserData
import com.madalin.notelo.util.DBCollection
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ArticleViewerViewModel : ViewModel() {
    val firestore by lazy { Firebase.firestore }

    // data holders to observe
    val ifNoteAddedSuccessfullyLiveData by lazy { MutableLiveData<Boolean>() }

    /**
     * Saves a given [Article] into the user's notes in [Firebase].
     * Notifies the observers about the adding state via [ifNoteAddedSuccessfullyLiveData].
     * @param article the article to save as a note
     */
    fun saveArticleAsNote(article: Article) {
        // creates a Note with the provided article data
        val newNote = Note(
            userId = UserData.currentUser.id,
            title = article.title,
            content = article.content
        )

        // adds the note to the database
        firestore.collection(DBCollection.NOTES)
            .add(newNote)
            .addOnSuccessListener { ifNoteAddedSuccessfullyLiveData.value = true }
            .addOnFailureListener { ifNoteAddedSuccessfullyLiveData.value = false }
    }

    /**
     * Formats the given date into the desired format.
     * @param givenDate date to format
     * @return formatted date
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun formatDate(givenDate: String?): String {
        val instant = Instant.parse(givenDate)
        val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")

        return localDateTime.format(formatter)
    }
}