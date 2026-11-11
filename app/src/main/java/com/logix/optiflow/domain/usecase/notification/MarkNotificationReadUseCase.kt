package com.logix.optiflow.domain.usecase.notification

import com.logix.optiflow.domain.repository.notification.NotificationRepository
import java.util.UUID
import javax.inject.Inject

class MarkNotificationReadUseCase @Inject constructor(
    private val repository: NotificationRepository,
) {
    suspend operator fun invoke(notificationId: UUID): Result<Unit> = repository.markRead(notificationId)
}
