package com.logix.optiflow.data.remote.dto.inventory

import java.util.UUID

data class SupplierDto(
    val id: UUID,
    val name: String,
    val contact: String?,
)

data class RegisterSupplierRequest(val name: String, val contact: String?)
