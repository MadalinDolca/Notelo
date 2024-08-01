package com.madalin.notelo.core.domain.result

import com.madalin.notelo.core.domain.model.Category
import com.madalin.notelo.core.domain.model.Note
import com.madalin.notelo.core.domain.model.Tag

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

sealed class MoveNoteResult {
    data object Success : MoveNoteResult()
    data class Error(val message: String?) : MoveNoteResult()
}

sealed class GetCategoryResult {
    data class Success(val category: Category) : GetCategoryResult()
    data class Error(val message: String?) : GetCategoryResult()
}

sealed class GetCategoriesResult {
    data class Success(val categories: List<Category>) : GetCategoriesResult()
    data class Error(val message: String?) : GetCategoriesResult()
}

sealed class GetTagsResult {
    data class Success(val tags: List<Tag>) : GetTagsResult()
    data class Error(val message: String?) : GetTagsResult()
}

sealed class TagsReplaceResult {
    data object Success : TagsReplaceResult()
    data class Error(val message: String?) : TagsReplaceResult()
}