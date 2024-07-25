package com.madalin.notelo.home.presentation

import androidx.lifecycle.ViewModel
import com.madalin.notelo.core.presentation.GlobalDriver

class HomeViewModel(
    private val globalDriver: GlobalDriver
) : ViewModel() {

    /**
     * Logs the user out via [GlobalDriver].
     */
    fun logout() {
        globalDriver.toggleUserLoginStatus(false)
    }
}