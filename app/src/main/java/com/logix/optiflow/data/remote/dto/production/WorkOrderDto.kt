package com.logix.optiflow.data.remote.dto.production

import java.time.Instant
import java.util.UUID

data class WorkOrderDto(
    val id: UUID,
    val patientId: UUID?,
    val opticalStoreId: UUID? = null,
    val status: String,
    val estimatedDeliveryDate: String? = null,
    val delayed: Boolean = false,
    val lenses: List<LensDto>? = null,
    val statusHistory: List<StatusChangeDto>? = null,
    val updatedAt: Instant?,
    val createdAt: Instant?,
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
