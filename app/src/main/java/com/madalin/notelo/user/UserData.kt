package com.madalin.notelo.user

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.madalin.notelo.ApplicationClass
import com.madalin.notelo.util.SharedPrefKey
import com.madalin.notelo.components.PopupBanner
import com.madalin.notelo.util.DBCollection

object UserData {
    private val sharedPreferences = ApplicationClass.context.getSharedPreferences(SharedPrefKey.USER_DATA, MODE_PRIVATE)

    val id get() = sharedPreferences.getString(SharedPrefKey.USER_DATA_ID, null).toString()
    val name get() = sharedPreferences.getString(SharedPrefKey.USER_DATA_NAME, null).toString()
    val email get() = sharedPreferences.getString(SharedPrefKey.USER_DATA_EMAIL, null).toString()
    val role get() = sharedPreferences.getString(SharedPrefKey.USER_DATA_ROLE, null).toString()

    /**
     * If the user is logged in, gets the current user's data from [Firestore][Firebase.firestore]
     * every time the data has changed and calls [storeUserData] to store it in [SharedPreferences].
     */
    fun getUserData() {
        Firebase.auth.currentUser?.let { it ->
            Firebase.firestore.collection(DBCollection.USERS).document(it.uid)
                .addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        PopupBanner.make(ApplicationClass.context, PopupBanner.TYPE_FAILURE, exception.message.toString()).show()
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        storeUserData(snapshot.toObject<User>()!!) // converts the data snapshot to User and stores it
                    }
                }
        }
    }

    /**
     * Stores the provided user's data in [SharedPreferences].
     * @param user data to store
     */
    fun storeUserData(user: User) {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()

        with(editor) {
            putString(SharedPrefKey.USER_DATA_ID, user.id)
            putString(SharedPrefKey.USER_DATA_NAME, user.name)
            putString(SharedPrefKey.USER_DATA_EMAIL, user.email)
            putString(SharedPrefKey.USER_DATA_ROLE, user.role)
            apply()
        }
    }
}