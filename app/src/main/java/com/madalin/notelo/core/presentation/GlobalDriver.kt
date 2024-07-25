package com.madalin.notelo.core.presentation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.madalin.notelo.R
import com.madalin.notelo.core.domain.model.User
import com.madalin.notelo.core.domain.repository.FirebaseUserRepository
import com.madalin.notelo.core.domain.result.UserResult
import com.madalin.notelo.core.presentation.util.UiText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class GlobalDriver(
    private val userRepository: FirebaseUserRepository
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _isUserSignedIn = MutableLiveData(false)
    val isUserSignedIn: LiveData<Boolean> get() = _isUserSignedIn

    private val _currentUser = MutableLiveData(User())
    val currentUser: LiveData<User> get() = _currentUser

    private val _popupBannerMessage = MutableLiveData<Pair<Int, UiText>>()
    val popupBannerMessage: LiveData<Pair<Int, UiText>> get() = _popupBannerMessage

    /**
     * Checks if the current user is signed in and obtains the stored user ID.
     * @return `true` if signed in and has ID, `false` otherwise
     */
    fun isUserSignedIn(): Boolean {
        if (userRepository.isSignedIn()) {
            val userId = userRepository.getCurrentUserId()

            if (userId != null) {
                _isUserSignedIn.value = true
                _currentUser.value?.id = userId
                return true
            }
        }

        return false
    }

    /**
     * Starts listening for user data if the user is signed in.
     */
    fun listenForUserData(/*context: Context*/) {
        if (isUserSignedIn()) {
            startListeningForUserData(/*context*/)
        }
    }

    /**
     * Starts listening for user data changes and updates [_currentUser].
     */
    private fun startListeningForUserData(/*context: Context*/) {
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
                                /*PopupBanner.make(
                                    context,
                                    PopupBanner.TYPE_FAILURE,
                                    context.getString(determineUserDataFetchingFailureMessage(result))
                                )*/
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
    fun setLoginStatus(isLoggedIn: Boolean) {
        _isUserSignedIn.value = isLoggedIn
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

        _popupBannerMessage.value = Pair(type, text)
    }
}