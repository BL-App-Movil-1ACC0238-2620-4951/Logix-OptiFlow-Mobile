package com.logix.optiflow.domain.model.production

import java.time.Instant
import java.util.UUID

data class WorkOrder(
    val id: UUID,
    val patientId: UUID?,
    val opticalStoreId: UUID?,
    val status: String,
    val estimatedDeliveryDate: String?,
    val delayed: Boolean,
    val lenses: List<Lens>,
    val statusHistory: List<StatusChange>,
    val updatedAt: Instant?,
    val createdAt: Instant?,
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
