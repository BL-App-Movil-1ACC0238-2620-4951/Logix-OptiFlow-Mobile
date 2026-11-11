package com.logix.optiflow.domain.usecase.searchbooking

import com.logix.optiflow.domain.model.searchbooking.StaffMember
import com.logix.optiflow.domain.repository.searchbooking.StaffMemberRepository
import java.util.UUID
import javax.inject.Inject

class RegisterStaffMemberUseCase @Inject constructor(
    private val repository: StaffMemberRepository,
) {
    suspend operator fun invoke(name: String, email: String, password: String, opticalStoreId: UUID, role: String): Result<StaffMember> =
        repository.register(name, email, password, opticalStoreId, role)
}
