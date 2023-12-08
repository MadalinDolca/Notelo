package com.madalin.notelo.feature.auth.passwordreset

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.madalin.notelo.R
import com.madalin.notelo.component.PopupBanner
import com.madalin.notelo.repository.FirebaseAuthRepository

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

        // if given data is not valid
        if (!validateField(email)) return

        repository.resetPassword(_email,
            onSuccess = {
                _popupMessageLiveData.value = Pair(PopupBanner.TYPE_INFO, R.string.check_your_email_to_reset_your_password)
            },
            onFailure = {
                _popupMessageLiveData.value = Pair(PopupBanner.TYPE_FAILURE, R.string.something_went_wrong_please_try_again)
            })
    }

    /**
     * Checks if the given [email] is valid. If not, it updates the data holders accordingly.
     * @return `true` if valid, `false` otherwise
     */
    private fun validateField(email: String): Boolean {
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
        }

        return true
    }
}