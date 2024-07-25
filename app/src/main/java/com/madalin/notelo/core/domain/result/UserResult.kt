package com.madalin.notelo.core.domain.result

import com.madalin.notelo.core.domain.model.User

sealed class UserResult {
    data class Success(val userData: User) : UserResult()
    data object NoUserId : UserResult()
    data object UserDataNotFound : UserResult()
    data object DataFetchingError : UserResult()
}