package com.logix.optiflow.domain.model.notification

import java.time.Instant

enum class NotificationType {
    APPOINTMENT,
    ORDER,
    HEALTH,
    PROMO,
}

data class NotificationItem(
    val id: String,
    val type: NotificationType,
    val title: String,
    val message: String,
    val occurredAt: Instant,
    val navigable: Boolean = false,
    val fromApi: Boolean = true,
)
