package tv.vizbee.screendemo.ui.signin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tv.vizbee.screendemo.auth.MvpdRegCodePoller
import tv.vizbee.screendemo.data.AuthRepository
import tv.vizbee.screendemo.data.RegCodePollResult
import tv.vizbee.screendemo.network.VizbeeAuthApiService

class SignInViewModel(application: Application) : AndroidViewModel(application) {
    private val authApiService = VizbeeAuthApiService.create()
    private val authRepository = AuthRepository(application, authApiService)
    private val regCodePoller = MvpdRegCodePoller(viewModelScope, authRepository)

    val regCode: LiveData<String> = regCodePoller.regCode

    private val _signInState = MutableLiveData<SignInState>()
    val signInState: LiveData<SignInState> = _signInState

    init {
        observeRegCodePoller()
    }

    private fun observeRegCodePoller() {
        regCodePoller.regCodeResult.observeForever { result ->
            when (result.status) {
                RegCodePollResult.Status.DONE -> _signInState.value = SignInState.Success
                RegCodePollResult.Status.ERROR -> _signInState.value = SignInState.Error(result.error ?: "Unknown error occurred")
                RegCodePollResult.Status.IN_PROGRESS -> _signInState.value = SignInState.Loading
                RegCodePollResult.Status.NOT_FOUND -> _signInState.value = SignInState.Loading
            }
        }

        regCodePoller.isCheckDone.observeForever { isDone ->
            if (isDone) {
                stopPolling()
            }
        }
    }

    fun requestCode() {
        viewModelScope.launch {
            try {
                _signInState.value = SignInState.Loading
                regCodePoller.requestCode()
            } catch (e: Exception) {
                _signInState.value = SignInState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun startPolling() {
        _signInState.value = SignInState.Loading
        regCodePoller.startPoll()
    }

    fun stopPolling() {
        regCodePoller.stopPoll()
    }

    override fun onCleared() {
        super.onCleared()
        regCodePoller.regCodeResult.removeObserver { }
        regCodePoller.isCheckDone.removeObserver { }
    }
}

sealed class SignInState {
    data object Loading : SignInState()
    data object Success : SignInState()
    data class Error(val message: String) : SignInState()
}