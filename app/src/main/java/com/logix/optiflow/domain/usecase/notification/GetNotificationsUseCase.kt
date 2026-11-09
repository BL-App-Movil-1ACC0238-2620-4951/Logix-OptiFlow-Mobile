package com.logix.optiflow.domain.usecase.notification

import com.logix.optiflow.domain.model.notification.NotificationItem
import com.logix.optiflow.domain.repository.notification.NotificationRepository
import java.util.UUID

class GetNotificationsUseCase(
    private val repository: NotificationRepository,
) {
    suspend operator fun invoke(patientId: UUID): List<NotificationItem> =
        repository.loadForPatient(patientId)
}
