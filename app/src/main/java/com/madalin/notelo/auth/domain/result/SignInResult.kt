package com.madalin.notelo.auth.domain.result

sealed class SignInResult {
    data object Success : SignInResult()
    data object UserNotFound : SignInResult()
    data object InvalidPassword : SignInResult()
    data class Error(val message: String?) : SignInResult()
}