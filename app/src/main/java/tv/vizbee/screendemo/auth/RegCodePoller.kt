package tv.vizbee.screendemo.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tv.vizbee.screendemo.data.RegCode
import tv.vizbee.screendemo.data.RegCodePollResult

abstract class RegCodePoller(private val scope: CoroutineScope) {
    private var pollJob: Job? = null

    private val _regCode = MutableLiveData<String>()
    val regCode: LiveData<String> = _regCode

    private val _regCodeResult = MutableLiveData<RegCodePollResult>()
    val regCodeResult: LiveData<RegCodePollResult> = _regCodeResult

    private val _isCheckDone = MutableLiveData(false)
    val isCheckDone: LiveData<Boolean> = _isCheckDone

    protected var isPollDone = false

    suspend fun requestCode(): String {
        isPollDone = false
        setIsCheckDone(false)

        return try {
            val regCode = doRequestCode()
            setRegCode(regCode.code)
            regCode.code
        } catch (exception: Exception) {
            // Handle exception
            throw exception
        }
    }

    fun startPoll() {
        stopPoll()
        pollJob = scope.launch {
            val code = regCode.value ?: run {
                requestCode()
            }
            startRegCodeProcess(code)
        }
    }

    fun stopPoll() {
        pollJob?.cancel()
        pollJob = null
    }

    protected abstract suspend fun doRequestCode(): RegCode
    protected abstract suspend fun startRegCodeProcess(regCode: String)
    protected abstract suspend fun pollRegCode(regCode: String)

    protected fun onRegCodePollResult(result: RegCodePollResult): Boolean {
        setRegCodeResult(result)
        when (result.status) {
            RegCodePollResult.Status.DONE, RegCodePollResult.Status.ERROR -> setIsCheckDone(true)
            else -> {} // Do nothing for other states
        }
        return isCheckDone.value == true
    }

    private suspend fun setRegCode(code: String) {
        withContext(Dispatchers.Main) {
            _regCode.value = code
        }
    }

    private fun setRegCodeResult(result: RegCodePollResult) {
        scope.launch {
            withContext(Dispatchers.Main) {
                _regCodeResult.value = result
            }
        }
    }

    private fun setIsCheckDone(isDone: Boolean) {
        scope.launch {
            withContext(Dispatchers.Main) {
                _isCheckDone.value = isDone
            }
        }
    }
}