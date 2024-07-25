package com.madalin.notelo.core.domain.util

/**
 * Holds constant values representing string length constraints used across the application.
 */
object LengthConstraint {
    // note
    val MIN_NOTE_TITLE_LENGTH = 3
    val MAX_NOTE_TITLE_LENGTH = 50

    val MIN_NOTE_CONTENT_LENGTH = 1
    val MAX_NOTE_CONTENT_LENGTH = 1000

    // tag
    val MIN_TAG_NAME_LENGTH = 2
    val MAX_TAG_NAME_LENGTH = 15
}