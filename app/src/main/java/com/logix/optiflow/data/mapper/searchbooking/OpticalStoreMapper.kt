package com.logix.optiflow.data.mapper.searchbooking

import com.logix.optiflow.data.remote.dto.searchbooking.OpticalStoreDto
import com.logix.optiflow.domain.model.searchbooking.OpticalStore

fun OpticalStoreDto.toDomain(): OpticalStore =
    OpticalStore(
        id = id,
        name = name,
        address = address,
        phone = phone,
        rating = rating,
        status = status,
    )
