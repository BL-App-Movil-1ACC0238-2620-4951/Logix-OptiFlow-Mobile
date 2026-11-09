package com.logix.optiflow.data.mapper.searchbooking

import com.logix.optiflow.data.remote.dto.searchbooking.TimeSlotDto
import com.logix.optiflow.domain.model.searchbooking.TimeSlot

fun TimeSlotDto.toDomain(): TimeSlot =
    TimeSlot(
        id = id,
        opticalStoreId = opticalStoreId,
        startDateTime = startDateTime,
        endDateTime = endDateTime,
        status = status,
    )
