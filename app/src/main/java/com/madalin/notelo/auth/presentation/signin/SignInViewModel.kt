package com.madalin.notelo.auth.presentation.signin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madalin.notelo.R
import com.madalin.notelo.auth.domain.repository.FirebaseAuthRepository
import com.madalin.notelo.auth.domain.result.SignInResult
import com.madalin.notelo.auth.domain.validation.AuthValidator
import com.madalin.notelo.core.presentation.components.PopupBanner
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class SignInViewModel(
    private val repository: FirebaseAuthRepository
) : ViewModel() {
    private val _isSignInSuccessLiveData = MutableLiveData<Boolean>()
    val isSignInSuccessLiveData: LiveData<Boolean> get() = _isSignInSuccessLiveData

    private val _emailErrorMessageLiveData = MutableLiveData<Int>()
    val emailErrorMessageLiveData: LiveData<Int> get() = _emailErrorMessageLiveData

    private val _passwordErrorMessageLiveData = MutableLiveData<Int>()
    val passwordErrorMessageLiveData: LiveData<Int> get() = _passwordErrorMessageLiveData

    private val _popupMessageLiveData = MutableLiveData<Pair<Int, Int>>()
    val popupMessageLiveData: LiveData<Pair<Int, Int>> get() = _popupMessageLiveData

    /**
     * Signs in the user with the given [email] and [password] if they are valid. Checks if the
     * [email] is verified and updates the data holders.
     */
    fun signIn(email: String, password: String) {
        val _email = email.trim()
        val _password = password.trim()

        // if given data is not valid
        if (!validateFields(_email, _password)) return

        // proceeds to sign in
        viewModelScope.launch {
            val result = async { repository.signInWithEmailAndPassword(_email, _password) }.await()
            when (result) {
                SignInResult.Success -> handleSignInSuccessResult()
                SignInResult.UserNotFound -> updateStateUponFailure(R.string.user_not_found)
                SignInResult.InvalidPassword -> updateStateUponFailure(R.string.invalid_password)
                SignInResult.Error -> updateStateUponFailure(R.string.login_error)
            }
        }
    }

    /**
     * Checks if the given [email] and [password] are valid. If not, it updates the data holders
     * accordingly.
     * @return `true` if valid, `false` otherwise
     */
    private fun validateFields(email: String, password: String): Boolean {
        val validationResult = AuthValidator.validateSignInFields(email, password)
        when (validationResult) {
            AuthValidator.SignInResult.Valid -> return true

            AuthValidator.SignInResult.EmptyEmail -> {
                _emailErrorMessageLiveData.value = R.string.email_cant_be_empty
                return false
            }

            AuthValidator.SignInResult.InvalidEmail -> {
                _emailErrorMessageLiveData.value = R.string.email_is_invalid
                return false
            }

            AuthValidator.SignInResult.InvalidPasswordLength -> {
                _passwordErrorMessageLiveData.value = R.string.password_is_too_short
                return false
            }

            AuthValidator.SignInResult.EmptyPassword -> {
                _passwordErrorMessageLiveData.value = R.string.password_cant_be_empty
                return false
            }
        }
    }

    /**
     * Sends a verification email if the email is not verified, otherwise sets the login status
     * to `true`.
     */
    private fun handleSignInSuccessResult() {
        if (repository.isEmailVerified()) {
            _isSignInSuccessLiveData.value = true
        } else {
            repository.sendEmailVerification()
            _popupMessageLiveData.value = Pair(PopupBanner.TYPE_INFO, R.string.check_your_email_to_confirm_your_account)
        }
    }

    /**
     * Updates the state holders upon a sign in failure with the given [message].
     */
    private fun updateStateUponFailure(message: Int) {
        _isSignInSuccessLiveData.value = false
        _popupMessageLiveData.value = Pair(PopupBanner.TYPE_FAILURE, message)
    }

    /**
     * Sets the user sign in data holder status to [status].
     * @param status `true` if user is signed in, `false` otherwise
     */
    fun setSignInStatus(status: Boolean) {
        _isSignInSuccessLiveData.value = status
    }
}