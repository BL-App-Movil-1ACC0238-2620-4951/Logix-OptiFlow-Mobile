package com.logix.optiflow.domain.model.notification

import java.time.Instant
import java.util.UUID

enum class NotificationType {
    APPOINTMENT,
    ORDER,
    HEALTH,
    PROMO,
    SURVEY,
}

data class NotificationItem(
    val id: UUID,
    val type: NotificationType,
    val title: String,
    val message: String,
    val occurredAt: Instant,
    val read: Boolean,
    val appointmentId: UUID? = null,
    val workOrderId: UUID? = null,
    /** Puntaje de la encuesta respondida; null si no es encuesta o falta responderla. */
    val surveyRating: Int? = null,
)
