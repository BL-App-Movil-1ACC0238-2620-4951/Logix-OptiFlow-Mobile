package com.logix.optiflow.domain.model.searchbooking

import java.util.UUID

data class OpticalStore(
    val id: UUID,
    val name: String,
    val address: String,
    val phone: String,
    val rating: Double?,
    val status: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
)
