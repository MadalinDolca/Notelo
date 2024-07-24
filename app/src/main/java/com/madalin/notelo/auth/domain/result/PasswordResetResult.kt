package com.madalin.notelo.auth.domain.result

sealed class PasswordResetResult {
    data object Success : PasswordResetResult()
    data object Error : PasswordResetResult()
}