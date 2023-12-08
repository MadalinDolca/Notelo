package com.madalin.notelo.repository

import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.madalin.notelo.feature.auth.signin.SignInFailure
import com.madalin.notelo.feature.auth.signup.SignUpFailure
import com.madalin.notelo.model.User
import com.madalin.notelo.util.DBCollection
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FirebaseAuthRepositoryImpl(
    private val externalScope: CoroutineScope = GlobalScope,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : FirebaseAuthRepository {
    private val auth = Firebase.auth
    private val firestore = Firebase.firestore

    override fun createUserWithEmailAndPassword(
        email: String, password: String,
        onSuccess: (FirebaseUser?) -> Unit, onFailure: (SignUpFailure) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess.invoke(auth.currentUser)
                } else {
                    when (task.exception) {
                        is FirebaseAuthInvalidUserException -> onFailure(SignUpFailure.InvalidEmail)
                        is FirebaseAuthInvalidCredentialsException -> onFailure(SignUpFailure.InvalidCredentials)
                        is FirebaseAuthUserCollisionException -> onFailure(SignUpFailure.UserAlreadyExists)
                        else -> onFailure(SignUpFailure.Error)
                    }
                }
            }
    }

    override fun storeAccountDataToFirestore(
        user: User,
        onSuccess: () -> Unit, onFailure: (String?) -> Unit
    ) {
        firestore.collection(DBCollection.USERS)
            .document(user.id) // adds user data into the document with the user id as a name
            .set(user)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onFailure(task.exception?.message)
                }
            }
    }

    override fun resetPassword(email: String, onSuccess: () -> Unit, onFailure: () -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onFailure()
                }
            }
    }

    override fun signInWithEmailAndPassword(
        email: String, password: String,
        onSuccess: () -> Unit, onFailure: (SignInFailure) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess.invoke()
                } else {
                    when (task.exception) {
                        is FirebaseAuthInvalidUserException -> onFailure(SignInFailure.UserNotFound)
                        is FirebaseAuthInvalidCredentialsException -> onFailure(SignInFailure.InvalidPassword)
                        else -> onFailure(SignInFailure.Error)
                    }
                }
            }
    }

    override fun getCurrentUserId() = auth.currentUser?.uid

    override fun isSignedIn(): Boolean {
        auth.currentUser?.getIdToken(true) // refresh token
        return auth.currentUser != null
    }

    override fun isEmailVerified() = auth.currentUser?.isEmailVerified == true

    override fun signOut(onComplete: (Boolean, String?) -> Unit) {
        externalScope.launch {
            try {
                auth.signOut()
                onComplete(true, null)
            } catch (e: Exception) {
                onComplete(false, e.message)
            }
        }
    }
}