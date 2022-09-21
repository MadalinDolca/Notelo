package com.madalin.notelo

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.madalin.notelo.databinding.ActivityRegisterBinding
import com.madalin.notelo.databinding.LayoutProgressDialogBinding
import com.madalin.notelo.EdgeToEdge.edgeToEdge
import com.madalin.notelo.EdgeToEdge.Spacing.*
import com.madalin.notelo.EdgeToEdge.Direction.*
import com.madalin.notelo.models.User

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
        edgeToEdge(this, binding.lottie, MARGIN, TOP)
        edgeToEdge(this, binding.textViewLogin, MARGIN, BOTTOM)

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

        // check the correctness of the data in the fields
        when {
            // email
            TextUtils.isEmpty(email) -> {
                binding.editTextEmail.error =
                    getString(R.string.email_cant_be_empty) // sets the error message
                binding.editTextEmail.requestFocus() // gives focus to this specific view
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

            // confirm password
            TextUtils.isEmpty(confirmPassword) -> {
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
                progressDialog.show()
                //progressDialog.setContentView(R.layout.layout_progress_dialog)
                progressDialog.window?.setBackgroundDrawableResource(android.R.color.transparent) // transparent background
                progressDialog.setContentView(R.layout.layout_progress_dialog)
                progressDialogBinding = LayoutProgressDialogBinding.inflate(progressDialog.layoutInflater) // get progress dialog views
                progressDialog.setContentView(progressDialogBinding.root)

                // initiate user account creation
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { userCreationTask ->
                        // if the user account has been created successfully
                        if (userCreationTask.isSuccessful) {
                            val user = User(email = email, role = User.DEFAULT_ROLE) // stores the data of the current user in a User object

                            auth.currentUser?.let { // if the current user is not null
                                firestore.collection("users")
                                    .document(it.uid) // adds it's related data into the document with the user id as a name
                                    .set(user)
                                    .addOnCompleteListener { addDatabaseTask ->
                                        // if the data has been successfully stored to the database
                                        if (addDatabaseTask.isSuccessful) {
                                            progressDialogBinding.progressBar.visibility = View.GONE // hides the spinner
                                            progressDialogBinding.lottie.visibility = View.VISIBLE // shows the lottie animation
                                            progressDialogBinding.textViewMessage.text = getString(R.string.you_have_successfully_registered) // sets a text message

                                            // removes the progress dialog and starts the LoginActivity
                                            Handler().postDelayed({
                                                progressDialog.dismiss()
                                                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                                                finish()
                                            }, 3000)
                                        }
                                        // if the data couldn't be added to the database
                                        else {
                                            Toast.makeText(
                                                this@RegisterActivity,
                                                "${getString(R.string.data_recording_error)}: ${userCreationTask.exception?.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            progressDialog.dismiss()
                                        }
                                    }
                            }
                        }
                        // if the user account couldn't be created
                        else {
                            Toast.makeText(
                                this@RegisterActivity,
                                "${getString(R.string.error_creating_account)}: ${userCreationTask.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }
    }
}