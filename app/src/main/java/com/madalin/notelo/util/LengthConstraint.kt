package com.madalin.notelo.util

/**
 * Holds constant values representing string length constraints used across the application.
 */
object LengthConstraint {
    // password
    val MIN_PASSWORD_LEGTH = 6
    val MAX_PASSWORD_LEGTH = 40

    // note
    val MIN_NOTE_TITLE_LENGTH = 3
    val MAX_NOTE_TITLE_LENGTH = 50

    val MIN_NOTE_CONTENT_LENGTH = 1
    val MAX_NOTE_CONTENT_LENGTH = 1000

    // category
    val MIN_CATEGORY_NAME_LENGTH = 3
    val MAX_CATEGORY_NAME_LENGTH = 30

    // tag
    val MIN_TAG_NAME_LENGTH = 2
    val MAX_TAG_NAME_LENGTH = 15
}