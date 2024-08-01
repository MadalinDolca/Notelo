package com.madalin.notelo.core.presentation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.madalin.notelo.R
import com.madalin.notelo.core.domain.model.User
import com.madalin.notelo.core.domain.repository.remote.FirebaseUserRepository
import com.madalin.notelo.core.domain.result.UserResult
import com.madalin.notelo.core.presentation.components.PopupBanner
import com.madalin.notelo.core.presentation.util.UiText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class GlobalDriver(
    private val userRepository: FirebaseUserRepository
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    val userId = userRepository.getCurrentUserId()

    private val _isUserSignedIn = MutableLiveData(false)
    val isUserSignedIn: LiveData<Boolean> get() = _isUserSignedIn

    private val _currentUser = MutableLiveData(User())
    val currentUser: LiveData<User> get() = _currentUser

    private val _popupBannerMessage = MutableLiveData<Pair<Int, UiText>>()
    val popupBannerMessage: LiveData<Pair<Int, UiText>> get() = _popupBannerMessage

    /**
     * Starts listening for user data if the user is signed in.
     */
    fun listenForUserData() {
        if (isUserSignedIn()) {
            startListeningForUserData()
        }
    }

    /**
     * Checks if the current user is signed in. If so, it obtains the stored user ID and marks the
     * user as signed in.
     * @return `true` if signed in and has ID, `false` otherwise
     */
    private fun isUserSignedIn(): Boolean {
        if (!userRepository.isSignedIn()) return false

        val userId = userRepository.getCurrentUserId()
        if (userId == null) {
            showPopupBanner(PopupBanner.TYPE_FAILURE, R.string.could_not_get_the_user_id)
            return false
        }

        val currentUser = _currentUser.value
        if (currentUser == null) {
            showPopupBanner(PopupBanner.TYPE_FAILURE, R.string.current_user_is_null)
            return false
        }

        _currentUser.value = currentUser.copy(id = userId)
        _isUserSignedIn.value = true
        return true
    }

    /**
     * Starts listening for user data changes and updates current user state.
     */
    private fun startListeningForUserData() {
        scope.launch {
            val result = launch {
                userRepository.observeUserData()
                    .collect { result ->
                        when (result) {
                            is UserResult.Success -> {
                                Log.d("GlobalDriver", "Obtained new user data: ${result.userData}")
                                _currentUser.postValue(result.userData)
                            }

                            UserResult.NoUserId, UserResult.DataFetchingError, UserResult.UserDataNotFound -> {
                                Log.d("GlobalDriver", "Could not get user data")
                                showPopupBanner(PopupBanner.TYPE_FAILURE, determineUserDataFetchingFailureMessage(result))
                            }
                        }
                    }
            }
            result.join() // keeps the coroutine running to listen for updates
        }
    }

    /**
     * Determines the [failureType] and returns the specific string resource ID.
     */
    private fun determineUserDataFetchingFailureMessage(failureType: UserResult) = when (failureType) {
        is UserResult.Success -> 0
        UserResult.DataFetchingError -> R.string.data_fetching_error
        UserResult.NoUserId -> R.string.could_not_get_the_user_id
        UserResult.UserDataNotFound -> R.string.user_data_not_found
    }

    /**
     * Sets the user login status to [isLoggedIn].
     */
    fun toggleUserLoginStatus(isLoggedIn: Boolean) {
        if (isLoggedIn) {
            _isUserSignedIn.value = true
            startListeningForUserData()
        } else {
            userRepository.signOut(
                onSuccess = { _isUserSignedIn.value = false },
                onFailure = { showPopupBanner(PopupBanner.TYPE_FAILURE, it ?: R.string.could_not_sign_out) }
            )
        }
    }

    /**
     * Displays a popup banner with the given [type] and [message].
     */
    fun showPopupBanner(type: Int, message: Any) {
        val text = when (message) {
            is String -> UiText.Dynamic(message)
            is Int -> UiText.Resource(message)
            else -> UiText.Empty
        }
        _popupBannerMessage.postValue(Pair(type, text))
    }
}