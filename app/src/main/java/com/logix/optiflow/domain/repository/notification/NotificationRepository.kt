package com.logix.optiflow.domain.repository.notification

import com.logix.optiflow.domain.model.notification.NotificationItem
import java.util.UUID

interface NotificationRepository {
    suspend fun loadForPatient(patientId: UUID): Result<List<NotificationItem>>

    suspend fun markRead(notificationId: UUID): Result<Unit>

    suspend fun answerSurvey(notificationId: UUID, rating: Int, comment: String?): Result<Unit>

    /** Registra el token push de este celular para el paciente. */
    suspend fun registerDevice(patientId: UUID): Result<Unit>
}
