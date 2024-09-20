package tv.vizbee.screendemo.auth

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import tv.vizbee.screendemo.data.RegCode
import tv.vizbee.screendemo.data.RegCodePollResult

abstract class RegCodePoller(private val scope: CoroutineScope) {
    private var pollJob: Job? = null

    private val _regCode = MutableLiveData<String>()
    val regCode: LiveData<String> = _regCode

    private val _isCheckDone = MutableLiveData(false)
    val isCheckDone: LiveData<Boolean> = _isCheckDone

    suspend fun requestCode(): String {
        setIsCheckDone(false)
        return try {
            val code = doRequestCode().code
            setRegCode(code)
            code
        } catch (exception: Exception) {
            Log.e(LOG_TAG, "Error requesting code: ${exception.message}")
            throw exception
        }
    }

    fun startPoll(regCode: String) {
        stopPoll()
        pollJob = scope.launch {
            startRegCodeProcess(regCode)
            setIsCheckDone(true)
        }
    }

    fun stopPoll() {
        pollJob?.cancel()
        pollJob = null
        setIsCheckDone(false)
    }

    protected abstract suspend fun doRequestCode(): RegCode
    protected abstract suspend fun startRegCodeProcess(regCode: String)
    protected abstract suspend fun pollRegCode(regCode: String)

    protected fun onRegCodePollResult(result: RegCodePollResult): Boolean {
        return result.status == RegCodePollResult.Status.DONE
    }

    private fun setRegCode(code: String) {
        _regCode.postValue(code)
    }

    private fun setIsCheckDone(isDone: Boolean) {
        _isCheckDone.postValue(isDone)
    }

    companion object {
        private const val LOG_TAG = "RegCodePoller"
    }
}