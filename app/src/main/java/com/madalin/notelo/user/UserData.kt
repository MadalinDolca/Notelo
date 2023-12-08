package com.madalin.notelo.user

import android.content.Context
import android.util.Log
import com.madalin.notelo.R
import com.madalin.notelo.component.PopupBanner
import com.madalin.notelo.model.User
import com.madalin.notelo.repository.FirebaseAuthRepository
import com.madalin.notelo.repository.FirebaseContentRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Object used to share user information across the app.
 */
object UserData : KoinComponent {
    private val authRepository: FirebaseAuthRepository by inject()
    private val contentRepository: FirebaseContentRepository by inject()

    /**
     * Used to hold and share the data of the current user.
     */
    var currentUser = User()

    /**
     * Checks if the current user is signed in and obtains the stored user ID.
     * @return `true` if signed in and has ID, `false` otherwise
     */
    fun isUserSignedIn(): Boolean {
        if (authRepository.isSignedIn()) {
            val userId = authRepository.getCurrentUserId()

            if (userId != null) {
                currentUser.id = userId
                return true
            }
        }

        return false
    }

    /**
     * Starts listening for user data changes and updates [currentUser].
     */
    fun startListeningForUserData(context: Context) {
        contentRepository.startListeningForUserData(
            onSuccess = {
                currentUser = it
                Log.d("UserData", "Obtained new user data")
            },
            onFailure = {
                when (it) {
                    UserFailure.DataFetchingError -> PopupBanner.make(context, PopupBanner.TYPE_FAILURE, context.getString(R.string.data_fetching_error))
                    UserFailure.NoUserId -> PopupBanner.make(context, PopupBanner.TYPE_FAILURE, context.getString(R.string.could_not_get_the_user_id))
                    UserFailure.UserDataNotFound -> PopupBanner.make(context, PopupBanner.TYPE_INFO, context.getString(R.string.user_data_not_found))
                }
            }
        )
    }
}