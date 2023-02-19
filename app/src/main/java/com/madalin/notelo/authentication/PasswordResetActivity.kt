package com.madalin.notelo.authentication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.madalin.notelo.R
import com.madalin.notelo.databinding.ActivityPasswordResetBinding
import com.madalin.notelo.utilities.EdgeToEdge.DIRECTION_BOTTOM
import com.madalin.notelo.utilities.EdgeToEdge.DIRECTION_TOP
import com.madalin.notelo.utilities.EdgeToEdge.SPACING_MARGIN
import com.madalin.notelo.utilities.EdgeToEdge.edgeToEdge
import com.madalin.notelo.utilities.PopupBanner

class PasswordResetActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPasswordResetBinding
    private var auth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordResetBinding.inflate(layoutInflater)
        setContentView(binding.root)
        edgeToEdge(this, binding.lottie, SPACING_MARGIN, DIRECTION_TOP)
        edgeToEdge(this, binding.textViewGoBackToLogin, SPACING_MARGIN, DIRECTION_BOTTOM)

        binding.buttonResetPassword.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()

            when {
                email.isEmpty() -> {
                    binding.editTextEmail.error = getString(R.string.email_cant_be_empty)
                    binding.editTextEmail.requestFocus()
                }

                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    binding.editTextEmail.error = getString(R.string.email_is_invalid)
                    binding.editTextEmail.requestFocus()
                }

                else -> {
                    auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            PopupBanner.make(
                                this@PasswordResetActivity, PopupBanner.TYPE_INFO,
                                getString(R.string.check_your_email_to_reset_your_password)
                            ).show()

                            finish()
                        } else {
                            PopupBanner.make(
                                this@PasswordResetActivity, PopupBanner.TYPE_FAILURE,
                                getString(R.string.something_went_wrong_please_try_again)
                            ).show()
                        }
                    }
                }
            }
        }

        binding.textViewGoBackToLogin.setOnClickListener {
            startActivity(Intent(this@PasswordResetActivity, LoginActivity::class.java))
            finish()
        }
    }
}