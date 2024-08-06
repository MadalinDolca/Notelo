package com.madalin.notelo.settings.presentation

import androidx.lifecycle.ViewModel
import com.madalin.notelo.core.presentation.GlobalDriver

class SettingsViewModel(
    private val globalDriver: GlobalDriver
) : ViewModel() {

    val userEmail = globalDriver.currentUser.value?.email

    /**
     * Logs the user out via [GlobalDriver].
     */
    fun logout() {
        globalDriver.toggleUserLoginStatus(false)
    }
}