package com.madalin.notelo.core.domain.result

sealed class UserResult {
    data object NoUserId : UserResult()
    data object UserDataNotFound : UserResult()
    data object DataFetchingError : UserResult()
}
