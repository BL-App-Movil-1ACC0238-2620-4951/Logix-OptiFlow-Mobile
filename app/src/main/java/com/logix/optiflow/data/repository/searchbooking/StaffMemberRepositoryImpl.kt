package com.logix.optiflow.data.repository.searchbooking

import com.logix.optiflow.data.mapper.searchbooking.toDomain
import com.logix.optiflow.data.remote.api.searchbooking.SearchBookingApi
import com.logix.optiflow.data.remote.safeApiCall
import com.logix.optiflow.domain.model.searchbooking.StaffMember
import com.logix.optiflow.domain.repository.searchbooking.StaffMemberRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StaffMemberRepositoryImpl @Inject constructor(
    private val api: SearchBookingApi,
) : StaffMemberRepository {

    override suspend fun getStaffMember(id: UUID): Result<StaffMember> =
        safeApiCall { api.getStaffMember(id).toDomain() }
}
