package com.logix.optiflow.data.remote.dto.searchbooking

import java.util.UUID

data class OpticalStoreDto(
    val id: UUID,
    val name: String,
    val address: String,
    val phone: String,
    val rating: Double?,
    val status: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
)
