package tv.vizbee.screendemo.ui.signin

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tv.vizbee.screendemo.auth.MvpdRegCodePoller
import tv.vizbee.screendemo.data.AuthRepository
import tv.vizbee.screendemo.data.RegCodePollResult
import tv.vizbee.screendemo.network.VizbeeAuthApiService
import tv.vizbee.screendemo.vizbee.homesso.SignInCallbackHolder

class SignInViewModel(
    application: Application,
    private val signInType: String
) : AndroidViewModel(application) {
    private val authApiService = VizbeeAuthApiService.create()
    private val authRepository = AuthRepository(application, authApiService)
    private val regCodePoller = MvpdRegCodePoller(viewModelScope, authRepository)

    val regCode: LiveData<String> = regCodePoller.regCode

    private val _signInState = MutableLiveData<SignInState>()
    val signInState: LiveData<SignInState> = _signInState

    private var isCheckDoneObserver: Observer<Boolean>? = null

    init {
        observeRegCodePoller()
    }

    private fun observeRegCodePoller() {
        isCheckDoneObserver = Observer { isDone ->
            if (isDone) {
                _signInState.value = SignInState.Success
                SignInCallbackHolder.getListener()?.onSuccess(signInType)
            }
        }
        regCodePoller.isCheckDone.observeForever(isCheckDoneObserver!!)
    }

    fun requestCode() {
        viewModelScope.launch {
            try {
                _signInState.value = SignInState.Loading
                val code = regCodePoller.requestCode()
                SignInCallbackHolder.getListener()?.onProgress(signInType, code)
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Error requesting code: ${e.message}", e)
                _signInState.value = SignInState.Error(e.message ?: "Unknown error occurred")
                SignInCallbackHolder.getListener()?.onFailure(signInType, false)
            }
        }
    }

    fun startPolling() {
        _signInState.value = SignInState.Loading
        regCodePoller.startPoll(regCode.value ?: "")
    }

    fun stopPolling() {
        regCodePoller.stopPoll()
    }

    override fun onCleared() {
        super.onCleared()
        isCheckDoneObserver?.let { regCodePoller.isCheckDone.removeObserver(it) }
    }

    companion object {
        private const val LOG_TAG = "SignInViewModel"
    }
}

sealed class SignInState {
    data object Loading : SignInState()
    data object Success : SignInState()
    data class Error(val message: String) : SignInState()
}

class SignInViewModelFactory(
    private val application: Application,
    private val signInType: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SignInViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SignInViewModel(application, signInType) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}