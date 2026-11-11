package com.logix.optiflow.data.repository.searchbooking

import com.logix.optiflow.data.mapper.searchbooking.toDomain
import com.logix.optiflow.data.remote.api.searchbooking.SearchBookingApi
import com.logix.optiflow.data.remote.dto.searchbooking.AssignStaffRoleRequest
import com.logix.optiflow.data.remote.dto.searchbooking.RegisterStaffMemberRequest
import com.logix.optiflow.data.remote.dto.searchbooking.UpdateStaffMemberStatusRequest
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

    override suspend fun getStaffMembers(opticalStoreId: UUID?): Result<List<StaffMember>> =
        safeApiCall { api.getStaffMembers(opticalStoreId).map { it.toDomain() } }

    override suspend fun register(
        name: String,
        email: String,
        password: String,
        opticalStoreId: UUID,
        role: String,
    ): Result<StaffMember> =
        safeApiCall { api.registerStaffMember(RegisterStaffMemberRequest(name, email, password, opticalStoreId, role)).toDomain() }

    override suspend fun assignRole(id: UUID, role: String): Result<StaffMember> =
        safeApiCall { api.assignStaffRole(id, AssignStaffRoleRequest(role)).toDomain() }

    override suspend fun updateStatus(id: UUID, active: Boolean): Result<StaffMember> =
        safeApiCall { api.updateStaffMemberStatus(id, UpdateStaffMemberStatusRequest(active)).toDomain() }
}
