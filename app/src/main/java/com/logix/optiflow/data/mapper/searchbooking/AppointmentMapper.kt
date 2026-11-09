package com.logix.optiflow.data.mapper.searchbooking

import com.logix.optiflow.data.remote.dto.searchbooking.AppointmentDto
import com.logix.optiflow.domain.model.searchbooking.Appointment

fun AppointmentDto.toDomain(): Appointment =
    Appointment(
        id = id,
        patientId = patientId,
        opticalStoreId = opticalStoreId,
        timeSlotId = timeSlotId,
        status = status,
        startDateTime = startDateTime,
        endDateTime = endDateTime,
    )
