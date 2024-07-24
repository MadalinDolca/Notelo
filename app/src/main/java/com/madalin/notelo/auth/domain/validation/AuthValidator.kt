package com.madalin.notelo.auth.domain.validation

import android.util.Patterns

object AuthValidator {
    const val MIN_PASSWORD_LENGTH = 6
    const val MAX_PASSWORD_LENGTH = 40

    /**
     * Validates the given [email], [password] and [confirmPassword] and returns a [SignUpResult].
     */
    fun validateSignUpFields(email: String, password: String, confirmPassword: String): SignUpResult {
        when (validateEmail(email)) {
            EmailResult.Empty -> return SignUpResult.EmptyEmail
            EmailResult.InvalidFormat -> return SignUpResult.InvalidEmail
            EmailResult.Valid -> {}
        }
        when (validatePassword(password)) {
            PasswordResult.Empty -> return SignUpResult.EmptyPassword
            PasswordResult.InvalidLength -> return SignUpResult.InvalidPasswordLength
            PasswordResult.Valid -> {}
        }
        when (validateConfirmPassword(password, confirmPassword)) {
            ConfirmPasswordResult.Empty -> return SignUpResult.EmptyConfirmPassword
            ConfirmPasswordResult.NotMatching -> return SignUpResult.PasswordsNotMatching
            ConfirmPasswordResult.Valid -> {}
        }
        return SignUpResult.Valid
    }

    /**
     * Validates the given [email] and [password] and returns a [SignInResult].
     */
    fun validateSignInFields(email: String, password: String): SignInResult {
        when (validateEmail(email)) {
            EmailResult.Empty -> return SignInResult.EmptyEmail
            EmailResult.InvalidFormat -> return SignInResult.InvalidEmail
            EmailResult.Valid -> {}
        }
        when (validatePassword(password)) {
            PasswordResult.Empty -> return SignInResult.EmptyPassword
            PasswordResult.InvalidLength -> return SignInResult.InvalidPasswordLength
            PasswordResult.Valid -> {}
        }
        return SignInResult.Valid
    }

    /**
     * Validates the given [email] and returns an [EmailResult].
     */
    fun validateEmail(email: String) = when {
        email.isEmpty() -> EmailResult.Empty
        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> EmailResult.InvalidFormat
        else -> EmailResult.Valid
    }

    /**
     * Validates the given [password] and returns a [PasswordResult].
     */
    fun validatePassword(password: String) = when {
        password.isEmpty() -> PasswordResult.Empty
        password.length < MIN_PASSWORD_LENGTH || password.length > MAX_PASSWORD_LENGTH -> PasswordResult.InvalidLength
        else -> PasswordResult.Valid
    }

    /**
     * Validates the given [password] and [confirmPassword] and returns a [ConfirmPasswordResult].
     */
    fun validateConfirmPassword(password: String, confirmPassword: String) = when {
        confirmPassword.isEmpty() -> ConfirmPasswordResult.Empty
        password != confirmPassword -> ConfirmPasswordResult.NotMatching
        else -> ConfirmPasswordResult.Valid
    }

    sealed class EmailResult {
        data object Valid : EmailResult()
        data object Empty : EmailResult()
        data object InvalidFormat : EmailResult()
    }

    sealed class PasswordResult {
        data object Valid : PasswordResult()
        data object Empty : PasswordResult()
        data object InvalidLength : PasswordResult()
    }

    sealed class ConfirmPasswordResult {
        data object Valid : ConfirmPasswordResult()
        data object Empty : ConfirmPasswordResult()
        data object NotMatching : ConfirmPasswordResult()
    }

    sealed class SignUpResult {
        data object Valid : SignUpResult()
        data object EmptyEmail : SignUpResult()
        data object InvalidEmail : SignUpResult()
        data object EmptyPassword : SignUpResult()
        data object InvalidPasswordLength : SignUpResult()
        data object EmptyConfirmPassword : SignUpResult()
        data object PasswordsNotMatching : SignUpResult()
    }

    sealed class SignInResult {
        data object Valid : SignInResult()
        data object EmptyEmail : SignInResult()
        data object InvalidEmail : SignInResult()
        data object EmptyPassword : SignInResult()
        data object InvalidPasswordLength : SignInResult()
    }
}