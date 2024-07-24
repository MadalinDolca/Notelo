package com.madalin.notelo.auth.presentation.signup

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
import com.madalin.notelo.core.presentation.components.AppProgressDialog
import com.madalin.notelo.core.presentation.components.PopupBanner
import com.madalin.notelo.databinding.FragmentSignUpBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class SignUpFragment : Fragment() {
    private val viewModel: SignUpViewModel by viewModel()
    private lateinit var binding: FragmentSignUpBinding
    private lateinit var navController: NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        edgeToEdge(activity, binding.lottie, SPACING_MARGIN, DIRECTION_TOP)
        edgeToEdge(activity, binding.textViewLogin, SPACING_MARGIN, DIRECTION_BOTTOM)

        navController = findNavController()

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        // sign up success observer
        viewModel.isSignUpSuccessLiveData.observe(viewLifecycleOwner) {
            if (it) {
                navController.navigate(R.id.action_signUpFragment_to_signInFragment)
                viewModel.setSignUpStatus(false) // set to false so that the user will still be able to access SignUpFragment
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

        // confirm password error observer
        viewModel.confirmPasswordErrorMessageLiveData.observe(viewLifecycleOwner) {
            binding.editTextConfirmPassword.error = getString(it)
            binding.editTextConfirmPassword.requestFocus()
        }

        // progress dialog visibility observer
        viewModel.isProgressDialogVisibleLiveData.observe(viewLifecycleOwner) {
            if (it) { // if visible
                AppProgressDialog.make(activity, getString(R.string.processing))
            } else {
                AppProgressDialog.dismiss()
            }
        }

        // progress dialog message observer
        viewModel.progressDialogMessageLiveData.observe(viewLifecycleOwner) {
            AppProgressDialog.update(it.first, getString(it.second))
        }

        // pop-up message observer
        viewModel.popupMessageLiveData.observe(viewLifecycleOwner) {
            PopupBanner.make(activity, it.first, getString(it.second)).show()
        }
    }

    private fun setupListeners() {
        // calls the signUp() method with the given email, password and confirm password
        binding.buttonCreateAccount.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()
            val confirmPassword = binding.editTextConfirmPassword.text.toString().trim()
            viewModel.signUp(email, password, confirmPassword)
        }

        // navigates to SignInFragment on click
        binding.textViewLogin.setOnClickListener {
            navController.navigate(R.id.action_signUpFragment_to_signInFragment)
        }
    }
}