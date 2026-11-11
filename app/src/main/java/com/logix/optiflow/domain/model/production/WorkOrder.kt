package com.logix.optiflow.domain.model.production

import java.time.Instant
import java.util.UUID

data class WorkOrder(
    val id: UUID,
    val saleId: UUID?,
    val patientId: UUID?,
    val opticalStoreId: UUID?,
    val status: String,
    val estimatedDeliveryDate: String?,
    val delayed: Boolean,
    val technician: NamedRef?,
    val laboratory: NamedRef?,
    val lenses: List<Lens>,
    val deliveryDelay: DeliveryDelay?,
    val statusHistory: List<StatusChange>,
    val updatedAt: Instant?,
    val createdAt: Instant?,
    val deliveredAt: Instant?,
)

/** Estados de la orden en el backend, en el orden del Kanban del taller. */
object WorkOrderStatus {
    const val PENDING = "PENDING"
    const val IN_WORKSHOP = "IN_WORKSHOP"
    const val QUALITY_CONTROL = "QUALITY_CONTROL"
    const val READY_FOR_DELIVERY = "READY_FOR_DELIVERY"
    const val DELIVERED = "DELIVERED"

    val ORDER = listOf(PENDING, IN_WORKSHOP, QUALITY_CONTROL, READY_FOR_DELIVERY, DELIVERED)

    /** Siguiente etapa; null si ya fue entregada. */
    fun next(status: String): String? = ORDER.getOrNull(ORDER.indexOf(status) + 1)
}

/** Técnico o laboratorio. */
data class NamedRef(
    val id: UUID,
    val name: String,
)

data class DeliveryDelay(
    val reason: String?,
    val newEstimatedDeliveryDate: String?,
)

data class Lens(
    val id: UUID?,
    val specifications: String?,
    val completed: Boolean,
)

data class StatusChange(
    val status: String,
    val changedAt: Instant?,
)
