package com.logix.optiflow.data.remote.dto.searchbooking

import java.util.UUID

data class StaffMemberDto(
    val id: UUID,
    val opticalStoreId: UUID,
    val name: String,
    val email: String,
    val role: String,
    val active: Boolean? = null,
)

data class RegisterStaffMemberRequest(
    val name: String,
    val email: String,
    val password: String,
    val opticalStoreId: UUID,
    val role: String,
)

data class AssignStaffRoleRequest(val role: String)

data class UpdateStaffMemberStatusRequest(val active: Boolean)
