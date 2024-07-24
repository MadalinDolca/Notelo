package com.madalin.notelo.auth.domain.result

import com.google.firebase.auth.FirebaseUser

sealed class SignUpResult {
    data class Success(val firebaseUser: FirebaseUser?) : SignUpResult()
    data object InvalidEmail : SignUpResult()
    data object InvalidCredentials : SignUpResult()
    data object UserAlreadyExists : SignUpResult()
    data class Error(val message: String?) : SignUpResult()
}

sealed class AccountDataStorageResult {
    data object Success : AccountDataStorageResult()
    data class Error(val message: String?) : AccountDataStorageResult()
}