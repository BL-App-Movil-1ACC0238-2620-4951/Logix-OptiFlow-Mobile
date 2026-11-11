package com.logix.optiflow.domain.usecase.searchbooking

import com.logix.optiflow.domain.model.searchbooking.StaffMember
import com.logix.optiflow.domain.repository.searchbooking.StaffMemberRepository
import java.util.UUID
import javax.inject.Inject

/** Un empleado inactivo ya no puede iniciar sesión. */
class UpdateStaffMemberStatusUseCase @Inject constructor(
    private val repository: StaffMemberRepository,
) {
    suspend operator fun invoke(id: UUID, active: Boolean): Result<StaffMember> = repository.updateStatus(id, active)
}
