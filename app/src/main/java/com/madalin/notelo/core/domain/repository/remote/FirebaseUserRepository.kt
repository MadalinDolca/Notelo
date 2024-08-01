package com.madalin.notelo.core.domain.repository.remote

import com.google.firebase.ktx.Firebase
import com.madalin.notelo.core.domain.result.UserResult
import kotlinx.coroutines.flow.Flow

interface FirebaseUserRepository {
    /**
     * Checks if the current Firebase user is signed in.
     * @return `true` if signed in, `false` otherwise
     */
    fun isSignedIn(): Boolean

    /**
     * Obtains the current user [Firebase] ID and returns it if it's not null.
     */
    fun getCurrentUserId(): String?

    /**
     * Obtains the current user data and starts listening for updates.
     */
    suspend fun observeUserData(): Flow<UserResult>

    /**
     * Signs out the currently authenticated user.
     * @param onSuccess function invoked when sign-out operation succeeds
     * @param onFailure function invoked when sign-out operation fails
     * - `String` parameter contains the error message
     */
    fun signOut(onSuccess: () -> Unit, onFailure: (String?) -> Unit)
}