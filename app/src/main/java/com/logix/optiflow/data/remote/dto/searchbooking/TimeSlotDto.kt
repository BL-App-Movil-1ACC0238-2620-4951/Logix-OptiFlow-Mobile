package com.logix.optiflow.data.remote.dto.searchbooking

import java.time.Instant
import java.util.UUID

data class TimeSlotDto(
    val id: UUID,
    val opticalStoreId: UUID,
    val startDateTime: Instant,
    val endDateTime: Instant,
    val status: String,
)

data class PublishTimeSlotsRequest(val timeSlots: List<SlotRequest>)

data class SlotRequest(val startDateTime: Instant, val endDateTime: Instant)
