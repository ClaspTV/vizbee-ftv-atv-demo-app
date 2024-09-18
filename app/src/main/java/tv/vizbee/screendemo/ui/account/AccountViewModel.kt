package tv.vizbee.screendemo.ui.account

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tv.vizbee.screendemo.data.AuthRepository
import tv.vizbee.screendemo.network.VizbeeAuthApiService
import tv.vizbee.screendemo.storage.SharedPreferencesManager

class AccountViewModel(application: Application) : AndroidViewModel(application) {
    private val authApiService = VizbeeAuthApiService.create()
    private val authRepository = AuthRepository(application, authApiService)
    private val sharedPreferencesManager = SharedPreferencesManager(application)

    private val _accountState = MutableLiveData<AccountState>()
    val accountState: LiveData<AccountState> = _accountState

    private val _signOutResult = MutableLiveData<SignOutResult>()
    val signOutResult: LiveData<SignOutResult> = _signOutResult

    init {
        checkSignInStatus()
    }

    private fun checkSignInStatus() {
        val email = sharedPreferencesManager.getStringValue("email")
        if (email.isNotEmpty()) {
            _accountState.value = AccountState.SignedIn(email)
        } else {
            _accountState.value = AccountState.SignedOut
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                val result = authRepository.signOut()
                if (result) {
                    sharedPreferencesManager.setStringValue("email", "")
                    sharedPreferencesManager.setStringValue("authToken", "")
                    _accountState.value = AccountState.SignedOut
                    _signOutResult.value = SignOutResult.Success
                } else {
                    _signOutResult.value = SignOutResult.Error("Sign out failed")
                }
            } catch (e: Exception) {
                _signOutResult.value = SignOutResult.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
}

sealed class AccountState {
    data class SignedIn(val email: String) : AccountState()
    object SignedOut : AccountState()
}

sealed class SignOutResult {
    object Success : SignOutResult()
    data class Error(val message: String) : SignOutResult()
}