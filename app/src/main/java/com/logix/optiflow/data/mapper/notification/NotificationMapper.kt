package com.logix.optiflow.data.mapper.notification

import com.logix.optiflow.data.remote.dto.notification.NotificationDto
import com.logix.optiflow.domain.model.notification.NotificationItem
import com.logix.optiflow.domain.model.notification.NotificationType
import java.time.Instant

fun NotificationDto.toDomain(): NotificationItem =
    NotificationItem(
        id = id,
        type = typeOf(type),
        title = title,
        message = message,
        // Un recordatorio aparece en su fecha programada; los demás, cuando se crearon.
        occurredAt = scheduledAt ?: createdAt ?: Instant.now(),
        read = status == "READ",
        appointmentId = appointmentId,
        workOrderId = workOrderId,
        surveyRating = surveyRating,
    )

private fun typeOf(raw: String): NotificationType =
    when (raw) {
        "APPOINTMENT_REMINDER" -> NotificationType.APPOINTMENT
        "LENS_ORDER_PROGRESS", "DELIVERY_DELAY" -> NotificationType.ORDER
        "ANNUAL_CHECKUP_REMINDER" -> NotificationType.HEALTH
        "BIRTHDAY_DISCOUNT" -> NotificationType.PROMO
        "SATISFACTION_SURVEY" -> NotificationType.SURVEY
        else -> NotificationType.HEALTH
    }
