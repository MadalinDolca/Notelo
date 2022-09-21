package com.madalin.notelo

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.madalin.notelo.EdgeToEdge.Direction.*
import com.madalin.notelo.EdgeToEdge.Spacing.*
import com.madalin.notelo.EdgeToEdge.edgeToEdge
import com.madalin.notelo.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var auth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater) // get activity views
        setContentView(binding.root) // setContentView(R.layout.activity_login)
        edgeToEdge(this, binding.lottie, MARGIN, TOP)
        edgeToEdge(this, binding.textViewSignUp, MARGIN, BOTTOM)

        binding.buttonLogin.setOnClickListener { loginUser() }

        // starts RegisterActivity upon clicking the "Sign up" TextView
        binding.textViewSignUp.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
            finish()
        }
    }

    /**
     * Gets and validate the data entered in the fields. If the data is correct,
     * [signInWithEmailAndPassword][com.google.firebase.auth.FirebaseAuth.signInWithEmailAndPassword]
     * is called for authentication. If the authentication was successful, it is checked whether the
     * email address of the account is verified.
     */
    fun loginUser() {
        // get fields data
        var email = binding.editTextEmail.text.toString().trim()
        var password = binding.editTextPassword.text.toString().trim()

        // checks the correctness of the data in the fields
        when {
            //email
            TextUtils.isEmpty(email) -> {
                binding.editTextEmail.error = getString(R.string.email_cant_be_empty)
                binding.editTextEmail.requestFocus()
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.editTextEmail.error = getString(R.string.email_is_invalid)
                binding.editTextEmail.requestFocus()
            }

            // password
            TextUtils.isEmpty(password) -> {
                binding.editTextPassword.error = getString(R.string.password_cant_be_empty)
                binding.editTextPassword.requestFocus()
            }
            password.length < 6 -> {
                binding.editTextPassword.error = getString(R.string.password_is_too_short)
                binding.editTextPassword.requestFocus()
            }

            // if the data is correct, proceed to registration in Firebase
            else -> {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { loginTask ->
                        // if the authentication was successful
                        if (loginTask.isSuccessful) {
                            // checks if the user's email address is confirmed
                            if (auth.currentUser?.isEmailVerified == true) {
                                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                finish()
                            } else {
                                auth.currentUser?.sendEmailVerification() // sends a verification email
                                Toast.makeText(this@LoginActivity, getString(R.string.check_your_email_to_confirm_your_account), Toast.LENGTH_SHORT).show()
                            }
                        }
                        // if the authentication failed
                        else {
                            Toast.makeText(this@LoginActivity, getString(R.string.login_error), Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }
}