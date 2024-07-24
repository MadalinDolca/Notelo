package com.madalin.notelo.auth.presentation.signup

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madalin.notelo.R
import com.madalin.notelo.auth.domain.result.SignUpResult
import com.madalin.notelo.core.presentation.components.AppProgressDialog
import com.madalin.notelo.core.presentation.components.PopupBanner
import com.madalin.notelo.core.domain.model.User
import com.madalin.notelo.auth.domain.repository.FirebaseAuthRepository
import com.madalin.notelo.core.domain.util.LengthConstraint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SignUpViewModel(
    private val repository: FirebaseAuthRepository
) : ViewModel() {
    private val _isSignUpSuccessLiveData = MutableLiveData<Boolean>()
    val isSignUpSuccessLiveData: LiveData<Boolean> get() = _isSignUpSuccessLiveData

    private val _emailErrorMessageLiveData = MutableLiveData<Int>()
    val emailErrorMessageLiveData: LiveData<Int> get() = _emailErrorMessageLiveData

    private val _passwordErrorMessageLiveData = MutableLiveData<Int>()
    val passwordErrorMessageLiveData: LiveData<Int> get() = _passwordErrorMessageLiveData

    private val _confirmPasswordErrorMessageLiveData = MutableLiveData<Int>()
    val confirmPasswordErrorMessageLiveData: LiveData<Int> get() = _confirmPasswordErrorMessageLiveData

    private val _isProgressDialogVisibleLiveData = MutableLiveData<Boolean>()
    val isProgressDialogVisibleLiveData: LiveData<Boolean> get() = _isProgressDialogVisibleLiveData

    private val _progressDialogMessageLiveData = MutableLiveData<Pair<Int, Int>>()
    val progressDialogMessageLiveData: LiveData<Pair<Int, Int>> get() = _progressDialogMessageLiveData

    private val _popupMessageLiveData = MutableLiveData<Pair<Int, Int>>()
    val popupMessageLiveData: LiveData<Pair<Int, Int>> get() = _popupMessageLiveData

    /**
     * Signs up the user with the given [email] and [password] if they are valid.
     * If the user's creation has been successfully performed, calls [storeUserToFirestore] to store
     * its data.
     */
    fun signUp(email: String, password: String, confirmPassword: String) {
        val _email = email.trim()
        val _password = password.trim()
        val _confirmPassword = confirmPassword.trim()

        // if given data is not valid
        if (!validateFields(_email, _password, _confirmPassword)) return

        _isProgressDialogVisibleLiveData.value = true // shows the progress dialog

        // proceeds to registration
        repository.createUserWithEmailAndPassword(_email, _password,
            onSuccess = { firebaseUser ->
                firebaseUser?.let { // if the current user is not null
                    storeUserToFirestore(User(id = it.uid, email = _email))
                }
            },
            onFailure = {
                handleAccountCreationFailure(it)
                _isProgressDialogVisibleLiveData.value = false // dismiss the progress dialog
            })
    }

    /**
     * Checks if the given [email], [password] and [confirmPassword] are valid. If not, it updates
     * the data holders accordingly.
     * @return `true` if valid, `false` otherwise
     */
    private fun validateFields(email: String, password: String, confirmPassword: String): Boolean {
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

            // confirm password is empty
            confirmPassword.isEmpty() -> {
                _confirmPasswordErrorMessageLiveData.value = R.string.password_cant_be_empty
                return false
            }

            // passwords don't match
            password != confirmPassword -> {
                _confirmPasswordErrorMessageLiveData.value = R.string.passwords_dont_match
                return false
            }
        }

        return true
    }

    /**
     * Stores the given [user] data to Firestore and updates the data holders.
     */
    private fun storeUserToFirestore(user: User) {
        repository.storeAccountDataToFirestore(user,
            onSuccess = {
                _progressDialogMessageLiveData.value = Pair(AppProgressDialog.TYPE_SUCCESS, R.string.you_have_successfully_registered) // updates the dialog
                viewModelScope.launch {
                    delay(3000)
                    _isProgressDialogVisibleLiveData.value = false // dismiss the progress dialog
                    _isSignUpSuccessLiveData.value = true // registered successfully
                }
            },
            onFailure = { errorMessage ->
                errorMessage?.let { Log.e("SignUpViewModel", it) }
                _popupMessageLiveData.value = Pair(PopupBanner.TYPE_FAILURE, R.string.data_recording_error)
                _isProgressDialogVisibleLiveData.value = false // dismiss the progress dialog
            })
    }

    /**
     * Determines the [failureType] of the account creation and handles it by updating the data holders.
     */
    private fun handleAccountCreationFailure(failureType: SignUpResult) {
        when (failureType) {
            SignUpResult.InvalidEmail -> {
                _isSignUpSuccessLiveData.value = false
                _popupMessageLiveData.value = Pair(PopupBanner.TYPE_FAILURE, R.string.email_is_invalid)
            }

            SignUpResult.InvalidCredentials -> {
                _isSignUpSuccessLiveData.value = false
                _popupMessageLiveData.value = Pair(PopupBanner.TYPE_FAILURE, R.string.invalid_credentials)
            }

            SignUpResult.UserAlreadyExists -> {
                _isSignUpSuccessLiveData.value = false
                _popupMessageLiveData.value = Pair(PopupBanner.TYPE_FAILURE, R.string.user_already_exists)
            }

            SignUpResult.Error -> {
                _isSignUpSuccessLiveData.value = false
                _popupMessageLiveData.value = Pair(PopupBanner.TYPE_FAILURE, R.string.error_creating_account)
            }
        }
    }

    /**
     * Sets the user sign up status to [status].
     * @param status `true` if user has signed up, `false` otherwise
     */
    fun setSignUpStatus(status: Boolean) {
        _isSignUpSuccessLiveData.value = status
    }
}