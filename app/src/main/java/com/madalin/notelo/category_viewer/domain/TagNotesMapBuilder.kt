package com.madalin.notelo.category_viewer.domain

import com.madalin.notelo.core.domain.model.Note
import com.madalin.notelo.core.domain.model.Tag

object TagNotesMapBuilder {
    /**
     * Builds a map of tags and notes based on the given [tagNotesMap] that has the first tag
     * containing all the notes in the category. Clears the "Untagged" tag if it exists.
     */
    fun buildTagNotesMap(tagNotesMap: Map<Tag, List<Note>>): Map<Tag, List<Note>> {
        val builtMap = mutableMapOf<Tag, List<Note>>()

        // prepares the "All notes" tag and list representing every note in the category
        val allTag = Tag.subAllNotes("") // tag
        val allNotes = tagNotesMap.values.flatten().toSet().toList() // flattens the note map and keeps unique notes

        // populates the new map
        builtMap.put(allTag, allNotes)
        builtMap.putAll(tagNotesMap)

        // removes the possible "Untagged" tag
        val untaggedKey = builtMap.keys.find { it.id == Tag.ID_UNTAGGED }
        untaggedKey?.let { builtMap.remove(it) }

        return builtMap
    }
}