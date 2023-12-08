package com.madalin.notelo.feature.auth.signin

/**
 * Sign in failure types.
 */
sealed class SignInFailure {
    object UserNotFound : SignInFailure()
    object InvalidPassword : SignInFailure()
    object Error : SignInFailure()
}