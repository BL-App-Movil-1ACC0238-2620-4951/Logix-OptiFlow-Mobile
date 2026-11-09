package com.logix.optiflow.data.remote

import okhttp3.Interceptor
import okhttp3.Response

/** Agrega `Authorization: Bearer <token>` cuando hay una sesión iniciada. */
class AuthInterceptor(
    private val tokenProvider: () -> String?,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider()
        val request =
            if (token.isNullOrBlank()) {
                chain.request()
            } else {
                chain.request().newBuilder().header("Authorization", "Bearer $token").build()
            }
        return chain.proceed(request)
    }
}
