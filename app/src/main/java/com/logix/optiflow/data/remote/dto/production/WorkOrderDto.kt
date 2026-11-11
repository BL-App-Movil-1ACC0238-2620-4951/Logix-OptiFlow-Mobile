package com.logix.optiflow.data.remote.dto.production

import java.time.Instant
import java.util.UUID

data class WorkOrderDto(
    val id: UUID,
    val saleId: UUID? = null,
    val patientId: UUID?,
    val opticalStoreId: UUID? = null,
    val status: String,
    val estimatedDeliveryDate: String? = null,
    val delayed: Boolean = false,
    val technician: NamedRefDto? = null,
    val laboratory: NamedRefDto? = null,
    val lenses: List<LensDto>? = null,
    val deliveryDelay: DeliveryDelayDto? = null,
    val statusHistory: List<StatusChangeDto>? = null,
    val updatedAt: Instant?,
    val createdAt: Instant?,
    val deliveredAt: Instant? = null,
)

/** Técnico o laboratorio: `id` y `name`. */
data class NamedRefDto(
    val id: UUID,
    val name: String,
)

data class DeliveryDelayDto(
    val reason: String?,
    val reportedAt: Instant?,
    val newEstimatedDeliveryDate: String?,
)

data class AssignTechnicianRequest(
    val technicianId: UUID,
)

data class SendToLaboratoryRequest(
    val laboratoryId: UUID,
)

/** `PATCH /work-orders/{id}/status` solo devuelve el estado nuevo. */
data class WorkOrderStatusDto(
    val id: UUID,
    val status: String,
    val updatedAt: Instant?,
)

data class UpdateWorkOrderStatusRequest(
    val status: String,
)

data class CompleteLensesRequest(
    val lensIds: List<UUID>?,
)

data class NotifyDelayRequest(
    val reason: String,
    val newEstimatedDeliveryDate: String,
)

data class LensDto(
    val id: UUID?,
    val specifications: String?,
    val completed: Boolean = false,
)

data class StatusChangeDto(
    val status: String,
    val changedAt: Instant?,
)
