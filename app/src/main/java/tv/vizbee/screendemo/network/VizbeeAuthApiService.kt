package tv.vizbee.screendemo.network

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface VizbeeAuthApiService {
    @POST("v1/accountregcode")
    @Headers("Content-Type: text/plain")
    suspend fun fetchAccountRegCode(@Body body: RequestBody): Response<ResponseBody>

    @POST("v1/accountregcode/poll")
    @Headers("Content-Type: text/plain")
    suspend fun pollAccountRegCodeStatus(@Body body: RequestBody): Response<ResponseBody>

    @POST("v1/signout")
    suspend fun signOut(@HeaderMap headers: Map<String, String>): Response<Unit>

    companion object {
        private const val BASE_URL = "https://homesso.vizbee.tv/"

        fun create(): VizbeeAuthApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(VizbeeAuthApiService::class.java)
        }
    }
}