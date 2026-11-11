package com.logix.optiflow.domain.repository.searchbooking

import com.logix.optiflow.domain.model.searchbooking.StaffMember
import java.util.UUID

interface StaffMemberRepository {
    suspend fun getStaffMember(id: UUID): Result<StaffMember>

    suspend fun getStaffMembers(opticalStoreId: UUID?): Result<List<StaffMember>>

    suspend fun register(name: String, email: String, password: String, opticalStoreId: UUID, role: String): Result<StaffMember>

    suspend fun assignRole(id: UUID, role: String): Result<StaffMember>

    suspend fun updateStatus(id: UUID, active: Boolean): Result<StaffMember>
}
