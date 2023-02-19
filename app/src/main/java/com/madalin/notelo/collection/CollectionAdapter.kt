package com.madalin.notelo.collection

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.madalin.notelo.databinding.LayoutBannerNoteBinding

class CollectionAdapter : RecyclerView.Adapter<CollectionAdapter.NotesViewHolder>() {
    private var notesList = mutableListOf<Note>()

    /**
     * Describes the view of an item and the metadata about its place in the [RecyclerView].
     */
    inner class NotesViewHolder(val binding: LayoutBannerNoteBinding) : ViewHolder(binding.root)

    /**
     * Returns a new [NotesViewHolder] with a binder inflated with the note's banner layout from
     * the XML when the [RecyclerView] needs it.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        val binding = LayoutBannerNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotesViewHolder(binding)
    }

    /**
     * Binds the data to the item view at the specified position when called by [RecyclerView].
     */
    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
        with(holder) {
            with(notesList[position]) {
                binding.textViewTitle.text = this.title // holder.binding.textViewTitle.text = notesList[position].title
                binding.textViewContent.text = this.content

                // if the note has an associated color it will set it's background and text color
                if (this.color.isNotEmpty()) {
                    val noteColor = Color.parseColor(this.color)
                    binding.relativeLayoutContainer.backgroundTintList = ColorStateList.valueOf(noteColor) // sets the background color

                    // sets the displayed text color according to the luminance of the note's color
                    if (ColorUtils.calculateLuminance(noteColor) < 0.2) { // if the color is dark
                        binding.textViewTitle.setTextColor(Color.WHITE)
                        binding.textViewContent.setTextColor(Color.WHITE)
                    } else { // if the color is light
                        binding.textViewTitle.setTextColor(Color.BLACK)
                        binding.textViewContent.setTextColor(Color.BLACK)
                    }
                }
            }
        }
    }

    /**
     * Returns the number of notes from the list.
     * @return [notesList]'s size
     */
    override fun getItemCount() = notesList.size

    fun setNotesList(newNotesList: List<Note>) {
        notesList.clear()
        notesList = newNotesList.toMutableList()
    }

    /**
     * Schimba culorile elementelor banner-ului daca modul intunecat este activat.
     *
     * @param holder detinatorul vederilor
     */
    /*private fun seteazaCuloriNightMode(holder: BannerSolicitareViewHolder) {
        if (Configuration.UI_MODE_NIGHT_YES === context.getResources().getConfiguration().uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            val colorStateList = ContextCompat.getColorStateList(context, R.color.fundal)
            holder.imageViewPersoana.setImageTintList(colorStateList)
            holder.imageViewCalendar.setImageTintList(colorStateList)
            holder.imageViewIconitaSolicitare.setImageTintList(colorStateList)
            holder.textViewNumeMelodie.setTextColor(colorStateList)
            holder.textViewNumePersoana.setTextColor(colorStateList)
            holder.textViewDataSolicitarii.setTextColor(colorStateList)
        }
    }*/
}