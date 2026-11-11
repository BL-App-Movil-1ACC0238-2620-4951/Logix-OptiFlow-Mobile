package com.logix.optiflow.domain.model.searchbooking

import java.util.UUID

data class StaffMember(
    val id: UUID,
    val opticalStoreId: UUID,
    val name: String,
    val email: String,
    val role: String,
    val active: Boolean = true,
)
