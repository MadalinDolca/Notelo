package com.madalin.notelo.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.work.WorkInfo
import com.madalin.notelo.R
import com.madalin.notelo.core.presentation.GlobalDriver
import com.madalin.notelo.core.presentation.components.PopupBanner

class SettingsViewModel(
    private val globalDriver: GlobalDriver
) : ViewModel() {

    val userEmail = globalDriver.currentUser.value?.email

    /**
     * Shows the synchronization status in a popup banner according to the given [state].
     */
    fun showSynchronizationStatus(state: WorkInfo.State) {
        when (state) {
            WorkInfo.State.ENQUEUED -> {}

            WorkInfo.State.RUNNING -> globalDriver.showPopupBanner(
                PopupBanner.TYPE_INFO,
                R.string.synchronization_has_started
            )

            WorkInfo.State.SUCCEEDED -> globalDriver.showPopupBanner(
                PopupBanner.TYPE_SUCCESS,
                R.string.synchronization_has_succeeded
            )

            WorkInfo.State.FAILED,
            WorkInfo.State.BLOCKED,
            WorkInfo.State.CANCELLED -> globalDriver.showPopupBanner(
                PopupBanner.TYPE_FAILURE,
                R.string.synchronization_has_failed
            )
        }
    }

    /**
     * Logs the user out via [GlobalDriver].
     */
    fun logout() {
        globalDriver.toggleUserLoginStatus(false)
    }
}