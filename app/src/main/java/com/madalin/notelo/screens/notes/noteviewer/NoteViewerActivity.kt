package com.madalin.notelo.screens.notes.noteviewer

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.madalin.notelo.R
import com.madalin.notelo.components.PopupBanner
import com.madalin.notelo.components.noteproperties.NotePropertiesBottomSheetDialog
import com.madalin.notelo.databinding.ActivityNoteViewerBinding
import com.madalin.notelo.models.Note
import com.madalin.notelo.user.UserData
import com.madalin.notelo.util.DBCollection
import com.madalin.notelo.util.EdgeToEdge.DIRECTION_BOTTOM
import com.madalin.notelo.util.EdgeToEdge.DIRECTION_TOP
import com.madalin.notelo.util.EdgeToEdge.SPACING_MARGIN
import com.madalin.notelo.util.EdgeToEdge.SPACING_PADDING
import com.madalin.notelo.util.EdgeToEdge.edgeToEdge
import com.madalin.notelo.util.Extra
import java.text.SimpleDateFormat
import java.util.*

/**
 * Activity used to view, update and create a note.
 */
class NoteViewerActivity : AppCompatActivity() {
    private val firestore = Firebase.firestore
    private var isEditEnabled = false
    private var noteData: Note? = null

    private lateinit var binding: ActivityNoteViewerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteViewerBinding.inflate(layoutInflater) // binds this activity's views
        setContentView(binding.root)
        edgeToEdge(this, binding.editTextTitle, SPACING_MARGIN, DIRECTION_TOP)
        edgeToEdge(this, binding.layoutOptions, SPACING_PADDING, DIRECTION_BOTTOM)

        // gets the extra and determines the mode
        if (intent.hasExtra(Extra.NOTE)) {
            noteData = intent.getParcelableExtra(Extra.NOTE)!!
            initializeViewAndUpdateMode(noteData!!)
        } else {
            initializeCreateMode()
        }
    }

    /**
     * Initializes the note viewing and updating mode. Prepares the layout to allow the user to view and update the given [noteData].
     */
    private fun initializeViewAndUpdateMode(givenNote: Note) {
        binding.editTextTitle.setText(givenNote.title)
        binding.editTextTitle.isEnabled = false
        binding.editTextTitle.background = null

        binding.editTextContent.setText(givenNote.content)
        binding.editTextContent.isEnabled = false
        binding.editTextContent.background = null

        // sets the last edited text according to today's date
        if (givenNote.updatedAt == null) {
            binding.textViewLastEdited.visibility = View.INVISIBLE
        } else if (givenNote.updatedAt?.before(Date()) == true) { // if before today
            binding.textViewLastEdited.text = givenNote.updatedAt?.let { "${getString(R.string.edited)} ${SimpleDateFormat("dd MMM yy").format(it)}" }
        } else if (givenNote.updatedAt?.equals(givenNote.updatedAt) == true) { // if today
            binding.textViewLastEdited.text = givenNote.updatedAt?.let { "${getString(R.string.edited)} ${SimpleDateFormat("hh:mm a").format(it)}" }
        }

        // edit button (enables/disables editing)
        binding.textViewAction.setOnClickListener {
            if (!isEditEnabled) { // if in not editable state
                // enables editing
                isEditEnabled = true
                binding.textViewAction.text = getString(R.string.done)

                binding.editTextTitle.isEnabled = true
                binding.editTextTitle.setBackgroundResource(R.drawable.background_rounded)

                binding.editTextContent.isEnabled = true
                binding.editTextContent.setBackgroundResource(R.drawable.background_rounded)
                binding.editTextContent.requestFocus()
            }
            // if in editable state
            else {
                // checks if the provided data is valid
                if (valitateFields()) {
                    // saves the changes
                    updateNote()

                    // disables editing
                    isEditEnabled = false
                    binding.textViewAction.text = getString(R.string.edit)

                    binding.editTextTitle.isEnabled = false
                    binding.editTextTitle.background = null

                    binding.editTextContent.isEnabled = false
                    binding.editTextContent.background = null
                }
            }
        }

        // properties button (shows the properties dialog)
        binding.textViewProperties.setOnClickListener {
            NotePropertiesBottomSheetDialog(this@NoteViewerActivity, givenNote).show()
        }
    }

    /**
     * Initializes the note creation mode. Prepares the layout to allow the user to create a new note.
     */
    private fun initializeCreateMode() {
        binding.textViewProperties.visibility = View.GONE
        binding.textViewAction.text = getString(R.string.create_note)

        binding.editTextTitle.isEnabled = true
        binding.editTextTitle.setBackgroundResource(R.drawable.background_rounded)

        binding.editTextContent.isEnabled = true
        binding.editTextContent.setBackgroundResource(R.drawable.background_rounded)

        binding.textViewAction.setOnClickListener {
            createNote()
        }
    }

    /**
     * Updates the given [noteData] with the provided data in the database.
     */
    private fun updateNote() {
        // if the provided data is valid
        if (valitateFields()) {
            val newData = mapOf(
                "title" to binding.editTextTitle.text.toString(),
                "content" to binding.editTextContent.text.toString()
            )

            // updates the given note locally
            noteData?.title = binding.editTextTitle.text.toString()
            noteData?.content = binding.editTextContent.text.toString()

            // updates the given note in the database
            firestore.collection(DBCollection.NOTES).document(noteData!!.id!!)
                .update(newData)
                .addOnSuccessListener {
                    PopupBanner.make(this, PopupBanner.TYPE_SUCCESS, getString(R.string.note_updated_successfully)).show()
                }
                .addOnFailureListener {
                    PopupBanner.make(this, PopupBanner.TYPE_FAILURE, getString(R.string.something_went_wrong_please_try_again)).show()
                }
        }
    }

    /**
     * Creates and adds a new [Note] to the database.
     */
    private fun createNote() {
        // if the provided data is valid
        if (valitateFields()) {
            val noteTitle = binding.editTextTitle.text.toString()
            val noteContent = binding.editTextContent.text.toString()
            val newNote = Note(userId = UserData.id, title = noteTitle, content = noteContent)

            // adds the note to the database
            firestore.collection(DBCollection.NOTES)
                .add(newNote)
                .addOnSuccessListener {
                    PopupBanner.make(this, PopupBanner.TYPE_SUCCESS, getString(R.string.note_created_successfully)).show()
                    finish() // ends the activity
                }
                .addOnFailureListener {
                    PopupBanner.make(this, PopupBanner.TYPE_FAILURE, getString(R.string.something_went_wrong_please_try_again)).show()
                }
        }
    }

    /**
     * Validates the provided [Note.title] and [Note.content].
     */
    private fun valitateFields(): Boolean {
        val noteTitle = binding.editTextTitle.text.toString()
        val noteContent = binding.editTextContent.text.toString()

        when {
            // if the title is too short
            noteTitle.length < 3 -> {
                binding.editTextTitle.error = getString(R.string.title_is_too_short)
                binding.editTextTitle.requestFocus()
            }

            // if the title is empty
            noteTitle.isEmpty() -> {
                binding.editTextTitle.error = getString(R.string.note_title_cant_be_empty)
                binding.editTextTitle.requestFocus()
            }

            // if the content is empty
            noteContent.isEmpty() -> {
                binding.editTextContent.error = getString(R.string.note_content_cant_be_empty)
                binding.editTextContent.requestFocus()
            }

            // if valid
            else -> return true
        }

        // if not valid
        return false
    }
}