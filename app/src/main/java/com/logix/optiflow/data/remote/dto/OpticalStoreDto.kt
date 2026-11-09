package com.logix.optiflow.data.remote.dto

import java.util.UUID

data class OpticalStoreDto(
    val id: UUID,
    val name: String,
    val address: String,
    val phone: String,
    val rating: Double?,
    val status: String,
)
