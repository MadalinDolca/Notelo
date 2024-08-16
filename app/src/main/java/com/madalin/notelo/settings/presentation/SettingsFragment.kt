package com.madalin.notelo.settings.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.madalin.notelo.R
import com.madalin.notelo.core.presentation.util.EdgeToEdge.DIRECTION_TOP
import com.madalin.notelo.core.presentation.util.EdgeToEdge.SPACING_MARGIN
import com.madalin.notelo.core.presentation.util.EdgeToEdge.edgeToEdge
import com.madalin.notelo.core.presentation.util.ThemeState
import com.madalin.notelo.databinding.FragmentSettingsBinding
import com.madalin.notelo.settings.domain.NotesSynchronizationWorker
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment() {
    private val viewModel: SettingsViewModel by viewModel()
    private lateinit var binding: FragmentSettingsBinding
    private lateinit var workManager: WorkManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        workManager = WorkManager.getInstance(requireContext())
    }

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
            startNotesSynchronization()
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

    /**
     * Starts the notes synchronization work request and observes its status.
     */
    private fun startNotesSynchronization() {
        val request = OneTimeWorkRequestBuilder<NotesSynchronizationWorker>()
            .setConstraints(
                Constraints(
                    requiredNetworkType = NetworkType.CONNECTED,
                    requiresBatteryNotLow = true
                )
            ).build()

        workManager.enqueueUniqueWork(
            NotesSynchronizationWorker.WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request
        )

        workManager.getWorkInfosForUniqueWorkLiveData(NotesSynchronizationWorker.WORK_NAME)
            .observe(viewLifecycleOwner) {
                it.forEach { workInfo ->
                    viewModel.showSynchronizationStatus(workInfo.state)
                }
            }
    }
}