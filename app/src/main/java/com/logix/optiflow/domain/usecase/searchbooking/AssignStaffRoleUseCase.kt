package com.logix.optiflow.domain.usecase.searchbooking

import com.logix.optiflow.domain.model.searchbooking.StaffMember
import com.logix.optiflow.domain.repository.searchbooking.StaffMemberRepository
import java.util.UUID
import javax.inject.Inject

/** El nuevo rol rige desde el siguiente inicio de sesión del empleado. */
class AssignStaffRoleUseCase @Inject constructor(
    private val repository: StaffMemberRepository,
) {
    suspend operator fun invoke(id: UUID, role: String): Result<StaffMember> = repository.assignRole(id, role)
}
