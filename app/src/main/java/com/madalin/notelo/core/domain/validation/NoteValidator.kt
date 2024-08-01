package com.madalin.notelo.core.domain.validation

object NoteValidator {
    const val MIN_NOTE_TITLE_LENGTH = 3
    const val MAX_NOTE_TITLE_LENGTH = 200

    const val MIN_NOTE_CONTENT_LENGTH = 1
    const val MAX_NOTE_CONTENT_LENGTH = 1000

    /**
     * Validates the given [title] and [content] and returns a [NoteResult].
     */
    fun validateFields(title: String, content: String): NoteResult {
        when (validateTitle(title)) {
            TitleResult.InvalidLength -> return NoteResult.InvalidTitleLength
            TitleResult.Valid -> {}
        }
        when (validateContent(content)) {
            ContentResult.InvalidLength -> return NoteResult.InvalidContentLength
            ContentResult.Valid -> {}
        }
        return NoteResult.Valid
    }

    /**
     * Validates the given [title] and returns a [TitleResult].
     */
    fun validateTitle(title: String) = when {
        title.length < MIN_NOTE_TITLE_LENGTH || title.length > MAX_NOTE_TITLE_LENGTH -> TitleResult.InvalidLength
        else -> TitleResult.Valid
    }

    /**
     * Validates the given [content] and returns a [ContentResult].
     */
    fun validateContent(content: String) = when {
        content.length < MIN_NOTE_CONTENT_LENGTH || content.length > MAX_NOTE_CONTENT_LENGTH -> ContentResult.InvalidLength
        else -> ContentResult.Valid
    }

    sealed class TitleResult {
        data object Valid : TitleResult()
        data object InvalidLength : TitleResult()
    }

    sealed class ContentResult {
        data object Valid : ContentResult()
        data object InvalidLength : ContentResult()
    }

    sealed class NoteResult {
        data object Valid : NoteResult()
        data object InvalidTitleLength : NoteResult()
        data object InvalidContentLength : NoteResult()
    }
}