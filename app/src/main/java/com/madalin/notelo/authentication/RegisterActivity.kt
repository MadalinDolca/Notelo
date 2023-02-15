package com.madalin.notelo.authentication

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Patterns
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.madalin.notelo.Collection.USERS
import com.madalin.notelo.R
import com.madalin.notelo.databinding.ActivityRegisterBinding
import com.madalin.notelo.databinding.LayoutProgressDialogBinding
import com.madalin.notelo.models.User
import com.madalin.notelo.ui.EdgeToEdge.DIRECTION_BOTTOM
import com.madalin.notelo.ui.EdgeToEdge.DIRECTION_TOP
import com.madalin.notelo.ui.EdgeToEdge.SPACING_MARGIN
import com.madalin.notelo.ui.EdgeToEdge.edgeToEdge
import com.madalin.notelo.ui.PopupBanner

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var progressDialogBinding: LayoutProgressDialogBinding

    private var auth = Firebase.auth
    private var firestore = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater) // get activity views
        setContentView(binding.root) // setContentView(R.layout.activity_register)
        edgeToEdge(this, binding.lottie, SPACING_MARGIN, DIRECTION_TOP)
        edgeToEdge(this, binding.textViewLogin, SPACING_MARGIN, DIRECTION_BOTTOM)

        // calls registerAccount() upon clicking the "Create Account" button
        binding.buttonCreateAccount.setOnClickListener { registerAccount() }

        // starts LoginActivity upon clicking the "Login" TextView and ends this one
        binding.textViewLogin.setOnClickListener {
            startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
            finish()
        }
    }

    /**
     * Get and validate the data entered in the fields. If the data is correct, the new Firebase
     * user is created using [createUserWithEmailAndPassword][com.google.firebase.auth.FirebaseAuth.createUserWithEmailAndPassword].
     * If the user's creation has been successfully performed, its data is added to the database.
     */
    private fun registerAccount() {
        // get fields data
        val email = binding.editTextEmail.text.toString().trim()
        val password = binding.editTextPassword.text.toString().trim()
        val confirmPassword = binding.editTextConfirmPassword.text.toString().trim()

        // validate the data in the fields
        when {
            // email
            email.isEmpty() -> {
                binding.editTextEmail.error = getString(R.string.email_cant_be_empty) // sets the error message
                binding.editTextEmail.requestFocus() // gives focus to this specific view
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.editTextEmail.error = getString(R.string.email_is_invalid)
                binding.editTextEmail.requestFocus()
            }

            // password
            password.isEmpty() -> {
                binding.editTextPassword.error = getString(R.string.password_cant_be_empty)
                binding.editTextPassword.requestFocus()
            }
            password.length < 6 -> {
                binding.editTextPassword.error = getString(R.string.password_is_too_short)
                binding.editTextPassword.requestFocus()
            }

            // confirm password
            confirmPassword.isEmpty() -> {
                binding.editTextConfirmPassword.error = getString(R.string.password_cant_be_empty)
                binding.editTextConfirmPassword.requestFocus()
            }
            password != confirmPassword -> {
                binding.editTextConfirmPassword.error = getString(R.string.passwords_dont_match)
            }

            // if the data is correct, proceed to registration in Firebase
            else -> {
                // prepare and show the progress dialog
                progressDialog = ProgressDialog(this@RegisterActivity)
                progressDialog.apply {
                    show()
                    window?.setBackgroundDrawableResource(android.R.color.transparent) // transparent background
                    setContentView(R.layout.layout_progress_dialog)

                    progressDialogBinding = LayoutProgressDialogBinding.inflate(progressDialog.layoutInflater) // get progress dialog views
                    progressDialog.setContentView(progressDialogBinding.root)
                }

                // initiate user account creation
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { userCreationTask ->
                        // if the user account has been created successfully
                        if (userCreationTask.isSuccessful) {
                            auth.currentUser?.let { // if the current user is not null
                                val user = User(key = it.uid, email = email) // stores the data of the current user in a User object

                                firestore.collection(USERS)
                                    .document(it.uid) // adds it's related data into the document with the user id as a name
                                    .set(user)
                                    .addOnCompleteListener { addDatabaseTask ->
                                        // if the data has been successfully stored to the database
                                        if (addDatabaseTask.isSuccessful) {
                                            progressDialogBinding.apply {
                                                progressBar.visibility = View.GONE // hides the spinner
                                                lottie.visibility = View.VISIBLE // shows the lottie animation
                                                textViewMessage.text = getString(R.string.you_have_successfully_registered) // sets a text message
                                            }

                                            // removes the progress dialog and starts the LoginActivity
                                            Handler().postDelayed({
                                                progressDialog.dismiss()
                                                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                                                finish()
                                            }, 3000)
                                        }
                                        // if the data couldn't be added to the database
                                        else {
                                            PopupBanner.make(
                                                this@RegisterActivity, PopupBanner.TYPE_FAILURE,
                                                "${getString(R.string.data_recording_error)}: ${userCreationTask.exception?.message}"
                                            ).show()

                                            progressDialog.dismiss()
                                        }
                                    }
                            }
                        }
                        // if the user account couldn't be created
                        else {
                            PopupBanner.make(
                                this@RegisterActivity, PopupBanner.TYPE_FAILURE,
                                "${getString(R.string.error_creating_account)}: ${userCreationTask.exception?.message}"
                            ).show()
                        }
                    }
            }
        }
    }
}