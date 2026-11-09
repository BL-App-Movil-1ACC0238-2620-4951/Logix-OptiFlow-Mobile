package com.logix.optiflow.domain.model.searchbooking

import java.time.Instant
import java.util.UUID

data class Appointment(
    val id: UUID,
    val patientId: UUID,
    val opticalStoreId: UUID,
    val timeSlotId: UUID,
    val status: String,
    val startDateTime: Instant,
    val endDateTime: Instant,
)
