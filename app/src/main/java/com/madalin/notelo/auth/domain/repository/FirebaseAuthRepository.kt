package com.madalin.notelo.auth.domain.repository

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.madalin.notelo.auth.domain.result.AccountDataStorageResult
import com.madalin.notelo.auth.domain.result.SignInResult
import com.madalin.notelo.auth.domain.result.SignUpResult
import com.madalin.notelo.core.domain.model.User

/**
 * Repository interface that contains authentication related methods for [Firebase].
 */
interface FirebaseAuthRepository {
    /**
     * Signs up the user with the given [email] and [password] and stores their data to
     * [Firestore][Firebase.firestore]. Returns the result as a [SignUpResult].
     */
    suspend fun createUserWithEmailAndPassword(email: String, password: String): SignUpResult

    /**
     * Stores the given [user data][user] into Firestore and returns a [AccountDataStorageResult].
     */
    suspend fun storeAccountDataToFirestore(user: User): AccountDataStorageResult

    /**
     * Resets the user password associated with the given [email].
     * @param onSuccess callback function that will be invoked when the reset process succeeded
     * @param onFailure callback function that will be invoked when the reset process failed
     */
    fun resetPassword(email: String, onSuccess: () -> Unit, onFailure: () -> Unit)

    /**
     * Signs in the user with the given [email] and [password].
     * @param onSuccess callback function that will be invoked when the authentication process succeeded
     * @param onFailure callback function that will be invoked when the authentication process failed
     * - [SignInResult] parameter contains the failure type
     */
    fun signInWithEmailAndPassword(
        email: String, password: String,
        onSuccess: () -> Unit, onFailure: (SignInResult) -> Unit
    )

    /**
     * Obtains the current user [Firebase] ID and returns it if it's not null.
     */
    fun getCurrentUserId(): String?

    /**
     * Checks if the current Firebase user is signed in.
     * @return `true` if signed in, `false` otherwise
     */
    fun isSignedIn(): Boolean

    /**
     * Checks if the current Firebase user email is verified.
     * @return `true` if verified, `false` otherwise
     */
    fun isEmailVerified(): Boolean

    /**
     * Signs out the currently authenticated user.
     * @param onComplete callback function invoked upon completion of the sign-out operation
     * - `Boolean` parameter indicates the success or failure of the sign-out operation
     * - `String` parameter contains the error message in case of failure
     */
    fun signOut(onComplete: (Boolean, String?) -> Unit)
}