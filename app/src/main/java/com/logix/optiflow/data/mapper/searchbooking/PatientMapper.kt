package com.logix.optiflow.data.mapper.searchbooking

import com.logix.optiflow.data.remote.dto.searchbooking.PatientDto
import com.logix.optiflow.domain.model.searchbooking.Patient

fun PatientDto.toDomain(): Patient =
    Patient(
        id = id,
        name = name,
        email = email,
        phone = phone,
        birthDate = birthDate,
        createdAt = createdAt,
    )
