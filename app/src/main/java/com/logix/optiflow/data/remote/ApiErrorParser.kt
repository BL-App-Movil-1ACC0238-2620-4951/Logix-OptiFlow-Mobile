package com.logix.optiflow.data.remote

import com.logix.optiflow.data.remote.dto.ApiErrorDto
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.IOException
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
        "This patient has already rated this optical store." to "Ya calificaste esta óptica antes.",
        "This email is already registered as staff." to "Ese correo pertenece a una cuenta del personal.",
        "Email address is invalid." to "El correo no es válido.",
        "A cancelled appointment cannot be registered." to "La cita fue cancelada; no se puede atender.",
        "The patient must be examined before generating a prescription." to "Primero registra el examen del paciente.",
        "A closed clinical record cannot be modified." to "La ficha clínica ya está cerrada.",
        "The clinical record has no optical prescription yet." to "El paciente todavía no tiene receta.",
        "Only a draft quotation can be modified." to "Solo se puede cambiar una cotización en borrador.",
        "A percentage discount cannot exceed 100." to "El descuento no puede pasar del 100 %.",
        "The discount cannot exceed the quotation subtotal." to "El descuento no puede ser mayor que el subtotal.",
        "A rejection reason is required." to "Escribe el motivo del rechazo.",
        "Only an approved quotation can generate a sale." to "Primero el paciente debe aprobar la cotización.",
        "This quotation already has a sale." to "Esta cotización ya tiene una venta.",
        "A payment was already recorded for this sale." to "Esta venta ya tiene un pago registrado.",
        "Only a paid sale can be closed." to "Primero registra el pago de la venta.",
        "The payment gateway is not available. Try again later." to "El pago en línea no está disponible ahora. Intenta más tarde o paga en la óptica.",
        "The payment gateway is not available." to "El pago en línea no está disponible ahora. Intenta más tarde o paga en la óptica.",
        "All lenses must be completed before quality control." to "Primero marca todos los lentes como fabricados.",
        "Lenses can only be completed while the work order is in the workshop." to "Los lentes se marcan cuando la orden está en el taller.",
        "A finished work order can no longer be reassigned." to "La orden ya está terminada; no se puede reasignar.",
        "Only a pending or in-workshop work order can be sent to a laboratory." to "Solo una orden pendiente o en taller se envía al laboratorio.",
        "A delivered work order cannot be delayed." to "La orden ya fue entregada.",
        "A delivered work order cannot change its status." to "La orden ya fue entregada.",
        "A delay reason is required." to "Escribe el motivo del retraso.",
        "A frame model with this SKU already exists." to "Ya existe una montura con ese SKU.",
        "Frame model was not found." to "No existe una montura con ese SKU.",
        "This optical store has no inventory." to "Tu óptica todavía no tiene inventario.",
        "Supplier was not found." to "El proveedor ya no existe. Elige otro.",
        "Price must be greater than zero." to "El precio debe ser mayor que cero.",
        "Price is too high." to "El precio es demasiado alto.",
        "Minimum stock cannot be negative." to "El stock mínimo no puede ser negativo.",
        "Moved quantity must be greater than zero." to "La cantidad debe ser mayor que cero.",
        "You cannot deactivate your own account." to "No puedes desactivar tu propia cuenta.",
        "A staff member with this email is already registered." to "Ya hay un miembro del personal con ese correo.",
        "This email is already registered as a patient." to "Ese correo ya pertenece a un paciente.",
        "Staff members cannot have the PATIENT role." to "El personal no puede tener el rol de paciente.",
        "Time slots cannot overlap." to "Ya hay turnos abiertos en ese rango. Elige otro horario.",
        "Time slot end must be after its start." to "La hora de fin debe ser posterior a la de inicio.",
        "The start date must not be after the end date." to "La fecha de inicio no puede ser posterior a la de fin.",
        "The payment gateway is not configured." to "El pago en línea no está disponible ahora. Intenta más tarde o paga en la óptica.",
        "The token has expired." to "Tu sesión expiró. Inicia sesión de nuevo.",
        "The token is invalid." to "Tu sesión no es válida. Inicia sesión de nuevo.",
        "Authentication is required." to "Inicia sesión para continuar.",
    )

private val errorAdapter =
    Moshi.Builder().add(KotlinJsonAdapterFactory()).build().adapter(ApiErrorDto::class.java)

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
                runCatching { errorAdapter.fromJson(it) }.getOrNull()?.message
            }
        return apiMessage?.let { knownMessages[it.trim()] } ?: messageForStatus(code())
    }
    val raw = message.orEmpty()
    if (raw.contains("10.0.2.2", ignoreCase = true)) {
        return "No se pudo conectar al servidor. Si usas la variante local, levanta el backend en tu PC (puerto 8080). " +
            "Si no, en Android Studio elige Build Variants → prodDebug para usar Render en la nube."
    }
    if (this is IOException) {
        // Sin internet la operación no llega al servidor (US20, escenario 2).
        return "Sin conexión a internet. Esta operación requiere conexión y no se envió."
    }
    return raw.ifBlank { "Error inesperado. Intenta de nuevo." }
}
