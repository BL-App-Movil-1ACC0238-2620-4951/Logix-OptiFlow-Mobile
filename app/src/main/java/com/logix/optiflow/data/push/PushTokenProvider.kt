package com.logix.optiflow.data.push

import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

/** Pide o borra el token de Firebase Cloud Messaging de este celular. */
@Singleton
class PushTokenProvider @Inject constructor() {

    suspend fun currentToken(): String = FirebaseMessaging.getInstance().token.await()

    /** Al cerrar sesión el token deja de servir y el backend lo descarta en el siguiente envío. */
    suspend fun deleteToken() {
        FirebaseMessaging.getInstance().deleteToken().await()
    }
}

private suspend fun <T> Task<T>.await(): T =
    suspendCancellableCoroutine { continuation ->
        addOnCompleteListener { task ->
            val error = task.exception
            if (error != null) continuation.resumeWithException(error) else continuation.resume(task.result)
        }
    }
