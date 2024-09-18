package tv.vizbee.screendemo.data

import android.content.Context
import android.provider.Settings
import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.*
import org.json.JSONObject
import tv.vizbee.screendemo.network.VizbeeAuthApiService
import tv.vizbee.screendemo.storage.SharedPreferencesManager

class AuthRepository(
    private val context: Context,
    private val apiService: VizbeeAuthApiService
) {
    private val sharedPreferencesManager = SharedPreferencesManager(context)

    suspend fun fetchAccountRegCode(): RegCode = withContext(Dispatchers.IO) {
        val deviceId = getDeviceId()
        val jsonBody = JSONObject().put("deviceId", deviceId).toString()
        val mediaType = "text/plain".toMediaType()
        val requestBody = jsonBody.toRequestBody(mediaType)

        val response = apiService.fetchAccountRegCode(requestBody)
        if (response.isSuccessful) {
            val responseBody = response.body()?.string()
            val jsonResponse = JSONObject(responseBody)
            Log.d("AuthRepository", "fetchAccountRegCode response: $response jsonResponse: $jsonResponse deviceId: $deviceId requestBody = $requestBody")
            val code = jsonResponse.getString("code")
            RegCode(code, /*, response.getLong("expires")*/) // Assuming 5 minutes expiry
        } else {
            throw Exception("Failed to fetch account reg code: ${response.code()}")
        }
    }

    suspend fun pollForRegCodeStatus(
        regCode: String,
        callback: (RegCodePollResult) -> Boolean,
        pollingInterval: Long? = null
    ) {
        withContext(Dispatchers.IO) {
            var isDone = false
            while (!isDone) {
                try {
                    val deviceId = getDeviceId()
                    val jsonBody = JSONObject().apply {
                        put("deviceId", deviceId)
                        put("regCode", regCode)
                    }.toString()
                    val mediaType = "text/plain".toMediaType()
                    val requestBody = jsonBody.toRequestBody(mediaType)

                    val response = apiService.pollAccountRegCodeStatus(requestBody)
                    if (response.isSuccessful) {
                        val responseBody = response.body()?.string()
                        val jsonResponse = JSONObject(responseBody)
                        val status = jsonResponse.getString("status")
                        val result = when {
                            status == "notFound" -> RegCodePollResult(RegCodePollResult.Status.NOT_FOUND)
                            jsonResponse.has("authToken") -> {
                                val authToken = jsonResponse.getString("authToken")
                                val email = jsonResponse.getString("email")
                                sharedPreferencesManager.setStringValue("authToken", authToken)
                                sharedPreferencesManager.setStringValue("email", email)
                                RegCodePollResult(RegCodePollResult.Status.DONE, authToken, email)
                            }
                            else -> RegCodePollResult(RegCodePollResult.Status.IN_PROGRESS)
                        }

                        isDone = callback(result)
                    } else {
                        throw Exception("Poll request failed: ${response.code()}")
                    }
                } catch (e: Exception) {
                    val errorResult = RegCodePollResult(RegCodePollResult.Status.ERROR, error = e.message)
                    isDone = callback(errorResult)
                }

                if (!isDone) {
                    delay(pollingInterval ?: 2000) // Default to 2 seconds if no interval provided
                }
            }
        }
    }

    suspend fun signOut(): Boolean = withContext(Dispatchers.IO) {
        try {
            val authToken = sharedPreferencesManager.getStringValue("authToken")
            val headers = mapOf("Authorization" to authToken)
            val response = apiService.signOut(headers)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    private fun getDeviceId(): String {
        val deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID)
        return "firetv:$deviceId"
    }
}

data class RegCode(val code: String/*, val expires: Long*/)

data class RegCodePollResult(
    val status: Status,
    val authToken: String? = null,
    val email: String? = null,
    val error: String? = null
) {
    enum class Status {
        NOT_FOUND,
        IN_PROGRESS,
        DONE,
        ERROR
    }
}