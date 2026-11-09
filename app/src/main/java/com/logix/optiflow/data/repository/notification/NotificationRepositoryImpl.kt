package com.logix.optiflow.data.repository.notification

import com.logix.optiflow.data.local.notification.NotificationReadStore
import com.logix.optiflow.data.remote.api.searchbooking.SearchBookingApi
import com.logix.optiflow.domain.model.notification.NotificationItem
import com.logix.optiflow.domain.model.notification.NotificationType
import com.logix.optiflow.domain.repository.notification.NotificationRepository
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotificationRepositoryImpl(
    private val api: SearchBookingApi,
    private val readStore: NotificationReadStore,
) : NotificationRepository {

    override suspend fun loadForPatient(patientId: UUID): List<NotificationItem> =
        withContext(Dispatchers.IO) {
            val fromApi = mutableListOf<NotificationItem>()

            runCatching {
                api.getPatientAppointments(patientId)
            }.getOrDefault(emptyList()).forEach { appt ->
                val whenLabel =
                    DateTimeFormatter.ofPattern("EEEE", Locale("es", "PE"))
                        .withZone(ZoneId.systemDefault())
                        .format(appt.startDateTime)
                val timeLabel =
                    DateTimeFormatter.ofPattern("HH:mm", Locale("es", "PE"))
                        .withZone(ZoneId.systemDefault())
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

            addDemoIfMissing(fromApi)
            val supplemental = supplementalNotifications()
            (fromApi + supplemental).sortedByDescending { it.occurredAt }
        }

    private fun addDemoIfMissing(fromApi: MutableList<NotificationItem>) {
        val now = Instant.now()
        if (fromApi.none { it.type == NotificationType.APPOINTMENT }) {
            fromApi +=
                NotificationItem(
                    id = "demo-appt-reminder",
                    type = NotificationType.APPOINTMENT,
                    title = "Recordatorio de cita",
                    message = "Tu evaluación visual es el martes a las 10:30.",
                    occurredAt = now.minusSeconds(7_200L),
                    fromApi = false,
                )
        }
        if (fromApi.none { it.type == NotificationType.ORDER }) {
            fromApi +=
                NotificationItem(
                    id = "demo-order-progress",
                    type = NotificationType.ORDER,
                    title = "Tu pedido avanzó",
                    message = "Tus lentes ya están en producción.",
                    occurredAt = now.minusSeconds(18_000L),
                    fromApi = false,
                )
        }
    }

    private fun supplementalNotifications(): List<NotificationItem> {
        val now = Instant.now()
        return listOf(
            NotificationItem(
                id = "health-last-check",
                type = NotificationType.HEALTH,
                title = "Cuida tu salud visual",
                message = "Han pasado 9 meses desde tu último control.",
                occurredAt = now.minusSeconds(86_400L),
                fromApi = false,
            ),
            NotificationItem(
                id = "promo-season",
                type = NotificationType.PROMO,
                title = "Descuento de Temporada",
                message = "Compra un armazón oftálmico y llévate el segundo al 50% de descuento.",
                occurredAt = now.minusSeconds(90_000L),
                navigable = true,
                fromApi = false,
            ),
        )
    }

    override suspend fun getReadIds(): Set<String> = readStore.readIds()

    override suspend fun markAllRead(ids: Collection<String>) {
        readStore.markAll(ids)
    }

    override suspend fun markRead(id: String) {
        readStore.markOne(id)
    }
}
