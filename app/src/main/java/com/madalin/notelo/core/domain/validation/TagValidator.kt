package com.madalin.notelo.core.domain.validation

object TagValidator {
    const val MIN_TAG_NAME_LENGTH = 3
    const val MAX_TAG_NAME_LENGTH = 20

    /**
     * Validates the given [name] and returns a [NameResult].
     */
    fun validateName(name: String) = when {
        name.isEmpty() -> NameResult.Empty
        name.length < MIN_TAG_NAME_LENGTH || name.length > MAX_TAG_NAME_LENGTH -> NameResult.InvalidLength
        else -> NameResult.Valid
    }

    sealed class NameResult {
        data object Valid : NameResult()
        data object Empty : NameResult()
        data object InvalidLength : NameResult()
    }
}