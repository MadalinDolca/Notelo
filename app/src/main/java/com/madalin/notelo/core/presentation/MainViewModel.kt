package com.madalin.notelo.core.presentation

import androidx.lifecycle.ViewModel

class MainViewModel(
    private val globalDriver: GlobalDriver
) : ViewModel() {
    val isSignedIn = globalDriver.isUserSignedIn
    val popupBannerMessage = globalDriver.popupBannerMessage

    init {
        globalDriver.listenForUserData()
    }
}