package com.madalin.notelo.user

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.madalin.notelo.Collection
import com.madalin.notelo.SharedPrefKey
import com.madalin.notelo.utilities.PopupBanner

object UserData {
    val user = User()

    /**
     * If the user is logged in, gets the current user's data from Firestore every time the data
     * changes and stores them in [user].
     */
    fun getUserData(context: Context) {
        Firebase.auth.currentUser?.let { it ->
            Firebase.firestore.collection(Collection.USERS).document(it.uid)
                .addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        PopupBanner.make(context, PopupBanner.TYPE_FAILURE, exception.message!!).show()
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val user = snapshot.toObject<User>()!! // converts the data snapshot to User
                        storeUserData(context, user)
                    }
                }
        }
    }

    /**
     * Stores user's data locally.
     * @param user data to store
     */
    fun storeUserData(context: Context, user: User) {
        val editor: SharedPreferences.Editor = context.getSharedPreferences(SharedPrefKey.USER_DATA, MODE_PRIVATE).edit()
        editor.apply {
            putString(SharedPrefKey.USER_DATA_NAME, user.name)
            putString(SharedPrefKey.USER_DATA_EMAIL, user.email)
            putString(SharedPrefKey.USER_DATA_ROLE, user.role)
            apply()
        }
    }
}