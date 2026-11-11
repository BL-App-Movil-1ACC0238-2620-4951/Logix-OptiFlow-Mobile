package com.logix.optiflow.domain.usecase.searchbooking

import com.logix.optiflow.domain.model.searchbooking.StaffMember
import com.logix.optiflow.domain.repository.searchbooking.StaffMemberRepository
import java.util.UUID
import javax.inject.Inject

/** Personal de una óptica, ordenado por nombre. */
class GetStaffMembersUseCase @Inject constructor(
    private val repository: StaffMemberRepository,
) {
    suspend operator fun invoke(opticalStoreId: UUID?): Result<List<StaffMember>> =
        repository.getStaffMembers(opticalStoreId).map { list -> list.sortedBy { it.name.lowercase() } }
}
