package com.logix.optiflow.domain.repository.searchbooking

import com.logix.optiflow.domain.model.searchbooking.StaffMember
import java.util.UUID

interface StaffMemberRepository {
    suspend fun getStaffMember(id: UUID): Result<StaffMember>
}
