package com.logix.optiflow.data.remote.dto.notification

import java.time.Instant
import java.util.UUID

data class NotificationDto(
    val id: UUID,
    val patientId: UUID?,
    val type: String,
    val title: String,
    val message: String,
    val status: String,
    val createdAt: Instant?,
    val scheduledAt: Instant?,
    val readAt: Instant?,
    val appointmentId: UUID?,
    val workOrderId: UUID?,
    val surveyRating: Int?,
    val surveyComment: String?,
    val answeredAt: Instant?,
)

data class UpdateNotificationRequest(
    val read: Boolean,
)

data class AnswerSatisfactionSurveyRequest(
    val rating: Int,
    val comment: String?,
)
