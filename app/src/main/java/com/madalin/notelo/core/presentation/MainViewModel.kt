package com.madalin.notelo.core.presentation

import androidx.lifecycle.ViewModel

class MainViewModel(
    private val globalDriver: GlobalDriver
) : ViewModel() {
    val isSignedIn = globalDriver.isUserSignedIn
    val popupBannerMessage = globalDriver.popupBannerMessage

    init {
        // starts listening for user data at launch if the user is signed in
        listenForUserData()
    }

    /**
     * Checks if the current user is signed in and obtains its data via [GlobalDriver].
     */
    fun listenForUserData() {
        globalDriver.listenForUserData()
    }
}