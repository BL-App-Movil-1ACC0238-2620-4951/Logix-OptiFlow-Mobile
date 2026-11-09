package com.logix.optiflow.domain.model.searchbooking

import java.time.Instant
import java.util.UUID

data class TimeSlot(
    val id: UUID,
    val opticalStoreId: UUID,
    val startDateTime: Instant,
    val endDateTime: Instant,
    val status: String,
)
