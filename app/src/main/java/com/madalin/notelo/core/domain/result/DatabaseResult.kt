package com.madalin.notelo.core.domain.result

import com.madalin.notelo.core.domain.model.Note

sealed class UpsertResult {
    data object Success : UpsertResult()
    data class Error(val message: String?) : UpsertResult()
}

sealed class UpdateResult {
    data object Success : UpdateResult()
    data class Error(val message: String?) : UpdateResult()
}

sealed class DeleteResult {
    data object Success : DeleteResult()
    data class Error(val message: String?) : DeleteResult()
}

sealed class GetNoteResult {
    data class Success(val note: Note) : GetNoteResult()
    data class Error(val message: String?) : GetNoteResult()
}

sealed class GetNotesResult {
    data class Success(val notes: List<Note>) : GetNotesResult()
    data class Error(val message: String?) : GetNotesResult()
}