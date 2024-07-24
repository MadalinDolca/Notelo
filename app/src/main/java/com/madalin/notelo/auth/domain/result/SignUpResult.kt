package com.madalin.notelo.auth.domain.result

sealed class SignUpResult {
    data object InvalidEmail : SignUpResult()
    data object InvalidCredentials : SignUpResult()
    data object UserAlreadyExists : SignUpResult()
    data object Error : SignUpResult()
}