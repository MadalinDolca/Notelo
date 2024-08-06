package com.madalin.notelo.settings.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.madalin.notelo.R
import com.madalin.notelo.core.presentation.util.EdgeToEdge.DIRECTION_TOP
import com.madalin.notelo.core.presentation.util.EdgeToEdge.SPACING_MARGIN
import com.madalin.notelo.core.presentation.util.EdgeToEdge.edgeToEdge
import com.madalin.notelo.core.presentation.util.ThemeState
import com.madalin.notelo.databinding.FragmentSettingsBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment() {
    private val viewModel: SettingsViewModel by viewModel()
    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        edgeToEdge(activity, binding.textViewHeader, SPACING_MARGIN, DIRECTION_TOP)

        if (viewModel.userEmail != null) {
            binding.textViewSignInAs.text = getString(R.string.signed_in_as_x, viewModel.userEmail)
        } else {
            binding.textViewSignInAs.visibility = View.GONE
        }

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            binding.rowSwitchTheme.setIcon(R.drawable.ic_light_mode)
            binding.rowSwitchTheme.setText(getString(R.string.enable_light_mode))
        } else {
            binding.rowSwitchTheme.setIcon(R.drawable.ic_dark_mode)
            binding.rowSwitchTheme.setText(getString(R.string.enable_dark_mode))
        }

        setupListeners()
    }

    private fun setupListeners() {
        // sync button
        binding.rowSyncNotes.setOnClickListener {
            // TODO: sync notes
        }

        // theme switcher button
        binding.rowSwitchTheme.setOnClickListener {
            activity?.let {
                ThemeState.switchMode(it)
                it.recreate()
            }
        }

        // sign out button
        binding.rowSignOut.setOnClickListener {
            viewModel.logout()
        }
    }
}