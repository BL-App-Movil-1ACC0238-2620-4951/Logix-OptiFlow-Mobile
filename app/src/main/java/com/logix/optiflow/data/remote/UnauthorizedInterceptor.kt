package com.logix.optiflow.data.remote

import okhttp3.Interceptor
import okhttp3.Response

/** Si una petición con token recibe 401, el token venció o no es válido: se avisa a [SessionEvents]. */
class UnauthorizedInterceptor(
    private val sessionEvents: SessionEvents,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        if (response.code == 401 && request.header("Authorization") != null) {
            AuthTokenHolder.token = null
            sessionEvents.notifyExpired()
        }
        return response
    }
}
