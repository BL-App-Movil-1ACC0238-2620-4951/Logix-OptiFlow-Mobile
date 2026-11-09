package com.logix.optiflow.data.remote

import com.logix.optiflow.data.remote.dto.ApiErrorDto
import com.logix.optiflow.di.NetworkModule
import retrofit2.HttpException

/**
 * El backend responde los errores en inglés; aquí se pasan al español. Los mensajes que el paciente
 * ve en los flujos principales tienen su propio texto y el resto se explica según el código HTTP.
 */
private val knownMessages =
    mapOf(
        "The credentials are invalid." to "Correo o contraseña incorrectos.",
        "A patient with this email is already registered." to "Ya existe una cuenta con este correo.",
        "The selected time slot is no longer available." to "Ese horario ya no está disponible. Elige otro.",
        "The optical store is not available." to "La óptica no está disponible en este momento.",
        "The token has expired." to "Tu sesión expiró. Inicia sesión de nuevo.",
        "The token is invalid." to "Tu sesión no es válida. Inicia sesión de nuevo.",
        "Authentication is required." to "Inicia sesión para continuar.",
    )

private fun messageForStatus(code: Int): String =
    when (code) {
        400 -> "Revisa los datos ingresados."
        401 -> "Inicia sesión para continuar."
        403 -> "No tienes permiso para hacer esta acción."
        404 -> "No encontramos lo que buscas."
        409 -> "No se pudo completar la acción por el estado actual. Actualiza e intenta de nuevo."
        in 500..599 -> "El servidor tuvo un problema. Intenta de nuevo en unos minutos."
        else -> "No se pudo completar la solicitud (código $code)."
    }

fun Throwable.toUserMessage(): String {
    if (this is HttpException) {
        val body = response()?.errorBody()?.string()
        val apiMessage =
            body?.takeIf { it.isNotBlank() }?.let {
                runCatching { NetworkModule.moshi().adapter(ApiErrorDto::class.java).fromJson(it) }.getOrNull()?.message
            }
        return apiMessage?.let { knownMessages[it.trim()] } ?: messageForStatus(code())
    }
    val raw = message.orEmpty()
    if (
        raw.contains("10.0.2.2", ignoreCase = true) ||
        raw.contains("failed to connect", ignoreCase = true) ||
        raw.contains("Unable to resolve host", ignoreCase = true)
    ) {
        return "No se pudo conectar al servidor. Si usas la variante local, levanta el backend en tu PC (puerto 8080). " +
            "Si no, en Android Studio elige Build Variants → prodDebug para usar Render en la nube."
    }
    return raw.ifBlank { "Error inesperado. Intenta de nuevo." }
}
