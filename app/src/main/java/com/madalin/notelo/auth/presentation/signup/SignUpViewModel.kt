package com.madalin.notelo.auth.presentation.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madalin.notelo.R
import com.madalin.notelo.auth.domain.repository.FirebaseAuthRepository
import com.madalin.notelo.auth.domain.result.AccountDataStorageResult
import com.madalin.notelo.auth.domain.result.SignUpResult
import com.madalin.notelo.auth.domain.validation.AuthValidator
import com.madalin.notelo.core.domain.model.User
import com.madalin.notelo.core.presentation.components.AppProgressDialog
import com.madalin.notelo.core.presentation.components.PopupBanner
import kotlinx.coroutines.async
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

        // if given data is not valid the registration fails
        if (!validateFields(_email, _password, _confirmPassword)) return

        // proceeds to registration
        viewModelScope.launch {
            _isProgressDialogVisibleLiveData.value = true // shows the progress dialog

            val result = async { repository.createUserWithEmailAndPassword(email, password) }.await()
            when (result) {
                // TODO handle null firebase user
                is SignUpResult.Success -> result.firebaseUser?.let {
                    storeUserToFirestore(User(id = it.uid, email = _email))
                }

                SignUpResult.InvalidEmail -> updateStateUponFailure(R.string.email_is_invalid)
                SignUpResult.InvalidCredentials -> updateStateUponFailure(R.string.invalid_credentials)
                SignUpResult.UserAlreadyExists -> updateStateUponFailure(R.string.user_already_exists)
                is SignUpResult.Error -> updateStateUponFailure(R.string.error_creating_account)
            }
        }
    }

    /**
     * Checks if the given [email], [password] and [confirmPassword] are valid. If not, it updates
     * the data holders accordingly.
     * @return `true` if valid, `false` otherwise
     */
    private fun validateFields(email: String, password: String, confirmPassword: String): Boolean {
        val validationResult = AuthValidator.validateSignUpFields(email, password, confirmPassword)
        when (validationResult) {
            AuthValidator.SignUpResult.Valid -> return true

            AuthValidator.SignUpResult.EmptyEmail -> {
                _emailErrorMessageLiveData.value = R.string.email_cant_be_empty
                return false
            }

            AuthValidator.SignUpResult.InvalidEmail -> {
                _emailErrorMessageLiveData.value = R.string.email_is_invalid
                return false
            }

            AuthValidator.SignUpResult.EmptyPassword -> {
                _passwordErrorMessageLiveData.value = R.string.password_cant_be_empty
                return false
            }

            AuthValidator.SignUpResult.InvalidPasswordLength -> {
                _passwordErrorMessageLiveData.value = R.string.password_is_too_short
                return false
            }

            AuthValidator.SignUpResult.EmptyConfirmPassword -> {
                _confirmPasswordErrorMessageLiveData.value = R.string.password_cant_be_empty
                return false
            }

            AuthValidator.SignUpResult.PasswordsNotMatching -> {
                _confirmPasswordErrorMessageLiveData.value = R.string.passwords_dont_match
                return false
            }
        }
    }

    /**
     * Stores the given [user] data to Firestore and updates the data holders.
     */
    private fun storeUserToFirestore(user: User) {
        viewModelScope.launch {
            val result = async { repository.storeAccountDataToFirestore(user) }.await()
            when (result) {
                AccountDataStorageResult.Success -> {
                    _progressDialogMessageLiveData.value = Pair(AppProgressDialog.TYPE_SUCCESS, R.string.you_have_successfully_registered) // updates the dialog
                    delay(3000)
                    _isProgressDialogVisibleLiveData.value = false // dismiss the progress dialog
                    _isSignUpSuccessLiveData.value = true // registered successfully
                }

                is AccountDataStorageResult.Error -> {
                    _popupMessageLiveData.value = Pair(PopupBanner.TYPE_FAILURE, R.string.data_recording_error)
                    _isProgressDialogVisibleLiveData.value = false // dismiss the progress dialog
                }
            }
        }
    }

    /**
     * Updates the state holders upon a registration failure with the given [message].
     */
    private fun updateStateUponFailure(message: Int) {
        _isProgressDialogVisibleLiveData.value = false // dismisses the progress dialog
        _isSignUpSuccessLiveData.value = false // marks registration as failed
        _popupMessageLiveData.value = Pair(PopupBanner.TYPE_FAILURE, message)
    }

    /**
     * Sets the user sign up status to [isSignedUp].
     */
    fun setSignUpStatus(isSignedUp: Boolean) {
        _isSignUpSuccessLiveData.value = isSignedUp
    }
}