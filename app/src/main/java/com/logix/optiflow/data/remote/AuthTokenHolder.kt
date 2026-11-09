package com.logix.optiflow.data.remote

/** Token JWT de la sesión actual; [AuthInterceptor] lo envía en cada petición. */
object AuthTokenHolder {
    @Volatile
    var token: String? = null
}
