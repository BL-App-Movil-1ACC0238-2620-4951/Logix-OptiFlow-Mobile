package com.logix.optiflow.data.repository.notification

import com.logix.optiflow.data.local.notification.NotificationReadStore
import com.logix.optiflow.data.remote.api.searchbooking.SearchBookingApi
import com.logix.optiflow.data.remote.safeApiCall
import com.logix.optiflow.domain.model.STORE_ZONE
import com.logix.optiflow.domain.model.notification.NotificationItem
import com.logix.optiflow.domain.model.notification.NotificationType
import com.logix.optiflow.domain.repository.notification.NotificationRepository
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val api: SearchBookingApi,
    private val readStore: NotificationReadStore,
) : NotificationRepository {

    override suspend fun loadForPatient(patientId: UUID): Result<List<NotificationItem>> =
        safeApiCall {
            val fromApi = mutableListOf<NotificationItem>()

            runCatching {
                api.getPatientAppointments(patientId)
            }.getOrDefault(emptyList()).forEach { appt ->
                val whenLabel =
                    DateTimeFormatter.ofPattern("EEEE", Locale("es", "PE"))
                        .withZone(STORE_ZONE)
                        .format(appt.startDateTime)
                val timeLabel =
                    DateTimeFormatter.ofPattern("HH:mm", Locale("es", "PE"))
                        .withZone(STORE_ZONE)
                        .format(appt.startDateTime)
                fromApi +=
                    NotificationItem(
                        id = "appt-${appt.id}",
                        type = NotificationType.APPOINTMENT,
                        title = "Recordatorio de cita",
                        message = "Tu evaluación visual es el $whenLabel a las $timeLabel.",
                        occurredAt = appt.createdAt ?: appt.startDateTime,
                        fromApi = true,
                    )
            }

            runCatching {
                api.getPatientWorkOrders(patientId)
            }.getOrDefault(emptyList()).forEach { order ->
                val body =
                    when (order.status.uppercase(Locale.US)) {
                        "IN_PRODUCTION", "PRODUCTION" -> "Tus lentes ya están en producción."
                        "READY", "READY_FOR_DELIVERY" -> "Tu pedido está listo para recoger."
                        "DELIVERED" -> "Tu pedido fue entregado."
                        else -> "Estado del pedido: ${order.status}."
                    }
                fromApi +=
                    NotificationItem(
                        id = "order-${order.id}",
                        type = NotificationType.ORDER,
                        title = "Tu pedido avanzó",
                        message = body,
                        occurredAt = order.updatedAt ?: order.createdAt ?: Instant.now(),
                        fromApi = true,
                    )
            }

            fromApi.sortedByDescending { it.occurredAt }
        }

    override suspend fun getReadIds(): Set<String> = readStore.readIds()

    override suspend fun markAllRead(ids: Collection<String>) {
        readStore.markAll(ids)
    }

    override suspend fun markRead(id: String) {
        readStore.markOne(id)
    }
}
