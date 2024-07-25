package com.madalin.notelo.auth.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import com.madalin.notelo.auth.domain.repository.FirebaseAuthRepository
import com.madalin.notelo.auth.domain.result.AccountDataStorageResult
import com.madalin.notelo.auth.domain.result.PasswordResetResult
import com.madalin.notelo.auth.domain.result.SignInResult
import com.madalin.notelo.auth.domain.result.SignUpResult
import com.madalin.notelo.core.domain.model.User
import com.madalin.notelo.core.domain.util.DBCollection
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
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

    override suspend fun resetPassword(email: String): PasswordResetResult {
        try {
            auth.sendPasswordResetEmail(email).await()
            return PasswordResetResult.Success
        } catch (e: Exception) {
            return PasswordResetResult.Error
        }
    }

    override suspend fun signInWithEmailAndPassword(email: String, password: String): SignInResult {
        try {
            auth.signInWithEmailAndPassword(email, password).await()
            return SignInResult.Success
        } catch (e: FirebaseAuthInvalidUserException) {
            return SignInResult.UserNotFound
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            return SignInResult.InvalidPassword
        } catch (e: Exception) {
            return SignInResult.Error(e.message)
        }
    }

    override fun isEmailVerified() = auth.currentUser?.isEmailVerified == true

    override fun sendEmailVerification() {
        auth.currentUser?.sendEmailVerification()
    }
}