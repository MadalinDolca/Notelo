package com.madalin.notelo.screens.notes.noteviewer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.madalin.notelo.R
import com.madalin.notelo.components.noteproperties.NotePropertiesBottomSheetDialog
import com.madalin.notelo.util.EdgeToEdge.DIRECTION_BOTTOM
import com.madalin.notelo.util.EdgeToEdge.DIRECTION_TOP
import com.madalin.notelo.util.EdgeToEdge.edgeToEdge
import com.madalin.notelo.databinding.ActivityNoteViewerBinding
import com.madalin.notelo.models.Note
import com.madalin.notelo.util.EdgeToEdge.SPACING_MARGIN
import com.madalin.notelo.util.Extra
import java.text.SimpleDateFormat
import java.util.*

class NoteViewerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoteViewerBinding
    private lateinit var noteData: Note
    private var isEditEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteViewerBinding.inflate(layoutInflater) // binds this activity's views
        setContentView(binding.root)
        edgeToEdge(this, binding.scrollViewContent, SPACING_MARGIN, DIRECTION_TOP)
        edgeToEdge(this, binding.layoutOptions, SPACING_MARGIN, DIRECTION_BOTTOM)

        // gets the data and populates the fields
        noteData = intent.getParcelableExtra(Extra.NOTE)!!
        binding.editTextTitle.setText(noteData.title)
        binding.editTextContent.setText(noteData.content)

        // sets the last edited text according to today's date
        if (noteData.updatedAt == null) {
            binding.textViewLastEdited.visibility = View.INVISIBLE
        } else if (noteData.updatedAt?.before(Date()) == true) { // if before today
            binding.textViewLastEdited.text = noteData.updatedAt?.let { "${getString(R.string.edited)} ${SimpleDateFormat("dd MMM yy").format(it)}" }
        } else if (noteData.updatedAt?.equals(noteData.updatedAt) == true) { // if today
            binding.textViewLastEdited.text = noteData.updatedAt?.let { "${getString(R.string.edited)} ${SimpleDateFormat("hh:mm a").format(it)}" }
        }

        // edit button (enables/disables editing)
        binding.textViewEditSave.setOnClickListener {
            if (!isEditEnabled) { // if in not editable state, enables editing
                isEditEnabled = true
                binding.textViewEditSave.text = getString(R.string.done)
                binding.editTextTitle.isEnabled = true
                binding.editTextContent.isEnabled = true
                binding.editTextContent.requestFocus()
            }
            // if in editable state, disables editing
            else {
                isEditEnabled = false
                binding.textViewEditSave.text = getString(R.string.edit)
                binding.editTextTitle.isEnabled = false
                binding.editTextContent.isEnabled = false
            }
        }

        // properties button (shows the properties dialog)
        binding.textViewProperties.setOnClickListener {
            NotePropertiesBottomSheetDialog(this@NoteViewerActivity, noteData).show()
        }
    }
}