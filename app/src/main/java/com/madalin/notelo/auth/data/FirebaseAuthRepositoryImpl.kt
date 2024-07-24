package com.madalin.notelo.auth.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import com.madalin.notelo.auth.domain.repository.FirebaseAuthRepository
import com.madalin.notelo.auth.domain.result.AccountDataStorageResult
import com.madalin.notelo.auth.domain.result.SignInResult
import com.madalin.notelo.auth.domain.result.SignUpResult
import com.madalin.notelo.core.domain.model.User
import com.madalin.notelo.core.domain.util.DBCollection
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepositoryImpl(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val externalScope: CoroutineScope = GlobalScope,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : FirebaseAuthRepository {

    override suspend fun createUserWithEmailAndPassword(email: String, password: String): SignUpResult {
        try {
            val user = auth.createUserWithEmailAndPassword(email, password).await().user
            return SignUpResult.Success(user)
        } catch (e: FirebaseAuthInvalidUserException) {
            return SignUpResult.InvalidEmail
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            return SignUpResult.InvalidCredentials
        } catch (e: FirebaseAuthUserCollisionException) {
            return SignUpResult.UserAlreadyExists
        } catch (e: Exception) {
            return SignUpResult.Error(e.message)
        }
    }

    override suspend fun storeAccountDataToFirestore(user: User): AccountDataStorageResult {
        val docRef = firestore.collection(DBCollection.USERS).document(user.id) // document reference with the same name as the user ID

        try {
            docRef.set(user).await() // adds user data into the document
            return AccountDataStorageResult.Success
        } catch (e: Exception) {
            return AccountDataStorageResult.Error(e.message)
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
        onSuccess: () -> Unit, onFailure: (SignInResult) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess.invoke()
                } else {
                    when (task.exception) {
                        is FirebaseAuthInvalidUserException -> onFailure(SignInResult.UserNotFound)
                        is FirebaseAuthInvalidCredentialsException -> onFailure(SignInResult.InvalidPassword)
                        else -> onFailure(SignInResult.Error)
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