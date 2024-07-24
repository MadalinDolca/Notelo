package com.madalin.notelo.auth.domain.result

sealed class SignInResult {
    data object Success : SignInResult()
    data object UserNotFound : SignInResult()
    data object InvalidPassword : SignInResult()
    data object Error : SignInResult()
}