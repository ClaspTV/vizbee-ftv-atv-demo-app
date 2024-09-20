package tv.vizbee.screendemo.auth

import kotlinx.coroutines.CoroutineScope
import tv.vizbee.screendemo.data.AuthRepository
import tv.vizbee.screendemo.data.RegCode

class MvpdRegCodePoller(
    scope: CoroutineScope,
    private val authRepository: AuthRepository,
    private val pollingInterval: Long = 2000
) : RegCodePoller(scope) {

    override suspend fun doRequestCode(): RegCode {
        return authRepository.fetchAccountRegCode()
    }

    override suspend fun startRegCodeProcess(regCode: String) {
        pollRegCode(regCode)
    }

    override suspend fun pollRegCode(regCode: String) {
        authRepository.pollForRegCodeStatus(
            regCode,
            { result ->
                onRegCodePollResult(result)
            },
            pollingInterval
        )
    }
}