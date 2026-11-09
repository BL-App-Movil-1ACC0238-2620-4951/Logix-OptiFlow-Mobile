package com.logix.optiflow.data.remote

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Ejecuta una llamada al API en IO y devuelve el error como [Result.failure] en vez de lanzarlo. */
suspend fun <T> safeApiCall(block: suspend () -> T): Result<T> =
    withContext(Dispatchers.IO) {
        try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
