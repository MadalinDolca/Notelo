package com.madalin.notelo.auth.presentation.signin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.madalin.notelo.R
import com.madalin.notelo.core.domain.util.EdgeToEdge.DIRECTION_BOTTOM
import com.madalin.notelo.core.domain.util.EdgeToEdge.DIRECTION_TOP
import com.madalin.notelo.core.domain.util.EdgeToEdge.SPACING_MARGIN
import com.madalin.notelo.core.domain.util.EdgeToEdge.edgeToEdge
import com.madalin.notelo.databinding.FragmentSignInBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class SingInFragment : Fragment() {
    private val viewModel: SignInViewModel by viewModel() // get koin ViewModel
    private lateinit var binding: FragmentSignInBinding
    private lateinit var navController: NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        edgeToEdge(activity, binding.lottie, SPACING_MARGIN, DIRECTION_TOP)
        edgeToEdge(activity, binding.textViewSignUp, SPACING_MARGIN, DIRECTION_BOTTOM)

        navController = findNavController()

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        // email error observer
        viewModel.emailErrorMessageLiveData.observe(viewLifecycleOwner) {
            binding.editTextEmail.error = getString(it)
            binding.editTextEmail.requestFocus()
        }

        // password error observer
        viewModel.passwordErrorMessageLiveData.observe(viewLifecycleOwner) {
            binding.editTextPassword.error = getString(it)
            binding.editTextPassword.requestFocus()
        }
    }

    private fun setupListeners() {
        // calls the signIn() method with the given email and password
        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()
            viewModel.signIn(email, password)
        }

        // navigates to SignUpFragment on click
        binding.textViewSignUp.setOnClickListener {
            navController.navigate(R.id.action_signInFragment_to_signUpFragment)
        }

        // navigates to PasswordResetFragment on click
        binding.textViewRecoverAccount.setOnClickListener {
            navController.navigate(R.id.action_signInFragment_to_passwordResetFragment)
        }
    }
}