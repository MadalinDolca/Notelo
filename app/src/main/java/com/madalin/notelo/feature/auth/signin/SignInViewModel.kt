package com.madalin.notelo.feature.auth.signin

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.madalin.notelo.R
import com.madalin.notelo.component.PopupBanner
import com.madalin.notelo.repository.FirebaseAuthRepository
import com.madalin.notelo.util.LengthConstraint

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
        repository.signInWithEmailAndPassword(_email, _password,
            onSuccess = {
                if (repository.isEmailVerified()) {
                    _isSignInSuccessLiveData.value = true
                } else {
                    _popupMessageLiveData.value = Pair(PopupBanner.TYPE_INFO, R.string.check_your_email_to_confirm_your_account)
                }
            },
            onFailure = {
                when (it) {
                    SignInFailure.UserNotFound -> {
                        _isSignInSuccessLiveData.value = false
                        _popupMessageLiveData.value = Pair(PopupBanner.TYPE_FAILURE, R.string.user_not_found)
                    }

                    SignInFailure.InvalidPassword -> {
                        _isSignInSuccessLiveData.value = false
                        _popupMessageLiveData.value = Pair(PopupBanner.TYPE_FAILURE, R.string.invalid_password)
                    }

                    SignInFailure.Error -> {
                        _isSignInSuccessLiveData.value = false
                        _popupMessageLiveData.value = Pair(PopupBanner.TYPE_FAILURE, R.string.login_error)
                    }
                }
            })
    }

    /**
     * Checks if the given [email] and [password] are valid. If not, it updates the data holders
     * accordingly.
     * @return `true` if valid, `false` otherwise
     */
    private fun validateFields(email: String, password: String): Boolean {
        when {
            // email is empty
            email.isEmpty() -> {
                _emailErrorMessageLiveData.value = R.string.email_cant_be_empty
                return false
            }

            // not email format
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _emailErrorMessageLiveData.value = R.string.email_is_invalid
                return false
            }

            // password is empty
            password.isEmpty() -> {
                _passwordErrorMessageLiveData.value = R.string.password_cant_be_empty
                return false
            }

            // password is too short
            password.length < LengthConstraint.MIN_PASSWORD_LEGTH -> {
                _passwordErrorMessageLiveData.value = R.string.password_is_too_short
                return false
            }
        }

        return true
    }

    /**
     * Sets the user sign in data holder status to [status].
     * @param status `true` if user is signed in, `false` otherwise
     */
    fun setSignInStatus(status: Boolean) {
        _isSignInSuccessLiveData.value = status
    }
}