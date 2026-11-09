package com.logix.optiflow.domain.repository.notification

import com.logix.optiflow.domain.model.notification.NotificationItem
import java.util.UUID

interface NotificationRepository {
    suspend fun loadForPatient(patientId: UUID): Result<List<NotificationItem>>

    suspend fun getReadIds(): Set<String>

    suspend fun markAllRead(ids: Collection<String>)

    suspend fun markRead(id: String)
}
