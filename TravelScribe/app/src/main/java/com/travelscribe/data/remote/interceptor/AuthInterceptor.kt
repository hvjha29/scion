package com.travelscribe.data.remote.interceptor

import com.travelscribe.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * OkHttp interceptor for adding authentication headers to API requests.
 */
class AuthInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer ${BuildConfig.LLM_API_KEY}")
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .header("X-Client-Version", BuildConfig.VERSION_NAME)
            .build()

        return chain.proceed(authenticatedRequest)
    }
}
