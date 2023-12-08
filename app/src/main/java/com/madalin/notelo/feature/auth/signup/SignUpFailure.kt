package com.madalin.notelo.feature.auth.signup

/**
 * Sign up failure types.
 */
sealed class SignUpFailure {
    object InvalidEmail : SignUpFailure()
    object InvalidCredentials : SignUpFailure()
    object UserAlreadyExists : SignUpFailure()
    object Error : SignUpFailure()
}