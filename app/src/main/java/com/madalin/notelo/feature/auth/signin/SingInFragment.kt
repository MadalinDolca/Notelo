package com.madalin.notelo.feature.auth.signin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.madalin.notelo.R
import com.madalin.notelo.component.PopupBanner
import com.madalin.notelo.databinding.FragmentSignInBinding
import com.madalin.notelo.util.EdgeToEdge.DIRECTION_BOTTOM
import com.madalin.notelo.util.EdgeToEdge.DIRECTION_TOP
import com.madalin.notelo.util.EdgeToEdge.SPACING_MARGIN
import com.madalin.notelo.util.EdgeToEdge.edgeToEdge
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
        // login success observer
        viewModel.isSignInSuccessLiveData.observe(viewLifecycleOwner) {
            if (it) {
                navController.navigate(R.id.action_signInFragment_to_mainFragment)
                viewModel.setSignInStatus(false)
            }
        }

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

        // pop-up message observer
        viewModel.popupMessageLiveData.observe(viewLifecycleOwner) {
            PopupBanner.make(activity, it.first, getString(it.second)).show()
        }
    }

    private fun setupListeners() {
        // calls the signIn() method with the given email and password
        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()
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