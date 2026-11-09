package com.logix.optiflow.data.remote.dto.searchbooking

import java.util.UUID

data class StaffMemberDto(
    val id: UUID,
    val opticalStoreId: UUID,
    val name: String,
    val email: String,
    val role: String,
)
