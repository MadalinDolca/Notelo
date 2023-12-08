package com.madalin.notelo.user

/**
 * User data fetching failure types.
 */
sealed class UserFailure {
    object NoUserId : UserFailure()
    object UserDataNotFound : UserFailure()
    object DataFetchingError : UserFailure()
}
