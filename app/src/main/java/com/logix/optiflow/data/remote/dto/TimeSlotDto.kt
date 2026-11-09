package com.logix.optiflow.data.remote.dto

import java.time.Instant
import java.util.UUID

data class TimeSlotDto(
    val id: UUID,
    val opticalStoreId: UUID,
    val startDateTime: Instant,
    val endDateTime: Instant,
    val status: String,
)
