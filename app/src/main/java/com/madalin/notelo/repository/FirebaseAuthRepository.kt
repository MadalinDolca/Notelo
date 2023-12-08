package com.madalin.notelo.repository

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.madalin.notelo.feature.auth.signin.SignInFailure
import com.madalin.notelo.feature.auth.signup.SignUpFailure
import com.madalin.notelo.model.User

/**
 * Repository interface that contains authentication related methods for [Firebase].
 */
interface FirebaseAuthRepository {
    /**
     * Signs up the user with the given [email] and [password] and store their data to [Firestore][Firebase.firestore].
     * @param onSuccess callback function that will be invoked when the registration process succeeded
     * - [FirebaseUser] parameter contains the stored user's profile information in Firebase
     * @param onFailure callback function that will be invoked when the registration process failed
     * - [SignUpFailure] parameter contains the failure type
     */
    fun createUserWithEmailAndPassword(
        email: String, password: String,
        onSuccess: (FirebaseUser?) -> Unit, onFailure: (SignUpFailure) -> Unit
    )

    /**
     * Stores the user data into Firestore.
     * @param onSuccess callback function that will be invoked when the storage process succeeded
     * @param onFailure callback function that will be invoked when the storage process failed
     * - [String] parameter contains the error message
     */
    fun storeAccountDataToFirestore(user: User, onSuccess: () -> Unit, onFailure: (String?) -> Unit)

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
     * - [SignInFailure] parameter contains the failure type
     */
    fun signInWithEmailAndPassword(
        email: String, password: String,
        onSuccess: () -> Unit, onFailure: (SignInFailure) -> Unit
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