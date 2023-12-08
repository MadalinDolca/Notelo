package com.madalin.notelo.feature.auth.passwordreset

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.madalin.notelo.R
import com.madalin.notelo.component.PopupBanner
import com.madalin.notelo.databinding.FragmentPasswordResetBinding
import com.madalin.notelo.util.EdgeToEdge.DIRECTION_BOTTOM
import com.madalin.notelo.util.EdgeToEdge.DIRECTION_TOP
import com.madalin.notelo.util.EdgeToEdge.SPACING_MARGIN
import com.madalin.notelo.util.EdgeToEdge.edgeToEdge
import org.koin.androidx.viewmodel.ext.android.viewModel

class PasswordResetFragment : Fragment() {
    private val viewModel: PasswordResetViewModel by viewModel()
    private lateinit var binding: FragmentPasswordResetBinding
    private lateinit var navController: NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentPasswordResetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        edgeToEdge(activity, binding.lottie, SPACING_MARGIN, DIRECTION_TOP)
        edgeToEdge(activity, binding.textViewGoBackToLogin, SPACING_MARGIN, DIRECTION_BOTTOM)

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

        // pop-up message observer
        viewModel.popupMessageLiveData.observe(viewLifecycleOwner) {
            PopupBanner.make(activity, it.first, getString(it.second)).show()
        }
    }

    private fun setupListeners() {
        // calls resetPassword() method with the given email
        binding.buttonResetPassword.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            viewModel.resetPassword(email)
        }

        // navigates to SignInFragment on click
        binding.textViewGoBackToLogin.setOnClickListener {
            navController.navigate(R.id.action_passwordResetFragment_to_signInFragment)
        }
    }
}