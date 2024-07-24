package com.madalin.notelo.auth.presentation.passwordreset

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madalin.notelo.R
import com.madalin.notelo.auth.domain.repository.FirebaseAuthRepository
import com.madalin.notelo.auth.domain.result.PasswordResetResult
import com.madalin.notelo.auth.domain.validation.AuthValidator
import com.madalin.notelo.core.presentation.components.PopupBanner
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class PasswordResetViewModel(
    private val repository: FirebaseAuthRepository
) : ViewModel() {
    private val _emailErrorMessageLiveData = MutableLiveData<Int>()
    val emailErrorMessageLiveData: LiveData<Int> get() = _emailErrorMessageLiveData

    private val _popupMessageLiveData = MutableLiveData<Pair<Int, Int>>()
    val popupMessageLiveData: LiveData<Pair<Int, Int>> get() = _popupMessageLiveData

    /**
     * Resets the user password associated with the given [email] and updates the data holder.
     */
    fun resetPassword(email: String) {
        val _email = email.trim()

        // if given data is not valid the operation fails
        if (!validateField(email)) return

        // proceeds to reset password
        viewModelScope.launch {
            val result = async { repository.resetPassword(_email) }.await()
            when (result) {
                PasswordResetResult.Success -> _popupMessageLiveData.value = Pair(PopupBanner.TYPE_INFO, R.string.check_your_email_to_reset_your_password)
                PasswordResetResult.Error -> _popupMessageLiveData.value = Pair(PopupBanner.TYPE_FAILURE, R.string.something_went_wrong_please_try_again)
            }
        }
    }

    /**
     * Checks if the given [email] is valid. If not, it updates the data holders accordingly.
     * @return `true` if valid, `false` otherwise
     */
    private fun validateField(email: String): Boolean {
        val validationResult = AuthValidator.validateEmail(email)
        when (validationResult) {
            AuthValidator.EmailResult.Valid -> return true

            AuthValidator.EmailResult.Empty -> {
                _emailErrorMessageLiveData.value = R.string.email_cant_be_empty
                return false
            }

            AuthValidator.EmailResult.InvalidFormat -> {
                _emailErrorMessageLiveData.value = R.string.email_is_invalid
                return false
            }
        }
    }
}