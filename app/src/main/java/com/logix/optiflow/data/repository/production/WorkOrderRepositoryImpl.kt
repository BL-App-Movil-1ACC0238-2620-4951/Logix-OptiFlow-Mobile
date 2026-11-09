package com.logix.optiflow.data.repository.production

import com.logix.optiflow.data.mapper.production.toDomain
import com.logix.optiflow.data.remote.api.searchbooking.SearchBookingApi
import com.logix.optiflow.data.remote.safeApiCall
import com.logix.optiflow.domain.model.production.WorkOrder
import com.logix.optiflow.domain.repository.production.WorkOrderRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkOrderRepositoryImpl @Inject constructor(
    private val api: SearchBookingApi,
) : WorkOrderRepository {

    override suspend fun getPatientWorkOrders(patientId: UUID): Result<List<WorkOrder>> =
        safeApiCall { api.getPatientWorkOrders(patientId).map { it.toDomain() } }

    override suspend fun getWorkOrders(): Result<List<WorkOrder>> =
        safeApiCall { api.getWorkOrders().map { it.toDomain() } }
}
