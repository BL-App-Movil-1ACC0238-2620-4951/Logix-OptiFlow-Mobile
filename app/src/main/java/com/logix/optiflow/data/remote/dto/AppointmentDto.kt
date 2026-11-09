package com.logix.optiflow.data.remote.dto

import java.time.Instant
import java.util.UUID

data class BookAppointmentRequest(
    val patientId: UUID,
    val opticalStoreId: UUID,
    val timeSlotId: UUID,
)

data class UpdateAppointmentStatusRequest(
    val status: String,
)

data class AppointmentListResponse(
    val value: List<AppointmentDto>?,
)

data class AppointmentDto(
    val id: UUID,
    val patientId: UUID,
    val opticalStoreId: UUID,
    val timeSlotId: UUID,
    val status: String,
    val startDateTime: Instant,
    val endDateTime: Instant,
    val createdAt: Instant?,
    val updatedAt: Instant?,
)
