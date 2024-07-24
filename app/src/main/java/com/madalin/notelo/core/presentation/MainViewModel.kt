package com.madalin.notelo.core.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.madalin.notelo.auth.domain.repository.FirebaseAuthRepository

class MainViewModel(
    private val repository: FirebaseAuthRepository
) : ViewModel() {
    private val _popupMessageLiveData = MutableLiveData<Pair<Int, Int>>()
    val popupMessageLiveData: LiveData<Pair<Int, Int>> get() = _popupMessageLiveData

    /*fun startListeningForUserData() {
        repository.startListeningForUserData(
            onSuccess = {
                ApplicationState.setSignInState(true)
                UserData.userData = it
            },
            onFailure = {
                when (it) {
                    UserFailure.DataFetchingError -> _popupMessageLiveData.value = Pair(PopupBanner.TYPE_FAILURE, R.string.data_fetching_error)
                    UserFailure.NoUserId -> _popupMessageLiveData.value = Pair(PopupBanner.TYPE_FAILURE, R.string.couldnt_get_user_id)
                    UserFailure.UserDataNotFound -> Pair(PopupBanner.TYPE_INFO, R.string.user_data_not_found)
                }
            }
        )
    }*/

    fun logout() {
        repository.signOut { isSuccess, errorMessage ->
            if (isSuccess) {

            } else {
                errorMessage?.let {

                }
            }
        }
    }
}