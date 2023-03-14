package com.madalin.notelo

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.madalin.notelo.screens.authentication.LoginActivity
import com.madalin.notelo.user.UserData

class SplashScreenActivity : AppCompatActivity() {
    private var auth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // checks if the current Firebase user is signed-in to start the MainActivity, otherwise starts LoginActivity
        Handler().postDelayed({
            auth.currentUser?.getIdToken(true)

            if (auth.currentUser != null) {
                UserData.getUserData()
                startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
            } else {
                startActivity(Intent(this@SplashScreenActivity, LoginActivity::class.java))
            }
            finish() // close the activity
        }, 500)
    }
}