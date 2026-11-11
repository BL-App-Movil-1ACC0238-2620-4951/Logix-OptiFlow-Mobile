package com.logix.optiflow.data.repository.production

import com.logix.optiflow.data.mapper.production.toDomain
import com.logix.optiflow.data.remote.api.production.ProductionApi
import com.logix.optiflow.data.remote.api.searchbooking.SearchBookingApi
import com.logix.optiflow.data.remote.dto.production.AssignTechnicianRequest
import com.logix.optiflow.data.remote.dto.production.CompleteLensesRequest
import com.logix.optiflow.data.remote.dto.production.NotifyDelayRequest
import com.logix.optiflow.data.remote.dto.production.SendToLaboratoryRequest
import com.logix.optiflow.data.remote.dto.production.UpdateWorkOrderStatusRequest
import com.logix.optiflow.data.remote.safeApiCall
import com.logix.optiflow.domain.model.production.NamedRef
import com.logix.optiflow.domain.model.production.WorkOrder
import com.logix.optiflow.domain.repository.production.WorkOrderRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkOrderRepositoryImpl @Inject constructor(
    private val api: SearchBookingApi,
    private val production: ProductionApi,
) : WorkOrderRepository {

    override suspend fun getPatientWorkOrders(patientId: UUID): Result<List<WorkOrder>> =
        safeApiCall { api.getPatientWorkOrders(patientId).map { it.toDomain() } }

    override suspend fun getWorkOrders(): Result<List<WorkOrder>> =
        safeApiCall { api.getWorkOrders().map { it.toDomain() } }

    override suspend fun getWorkOrder(id: UUID): Result<WorkOrder> =
        safeApiCall { production.getWorkOrder(id).toDomain() }

    override suspend fun assignTechnician(id: UUID, technicianId: UUID): Result<WorkOrder> =
        safeApiCall { production.assignTechnician(id, AssignTechnicianRequest(technicianId)).toDomain() }

    override suspend fun sendToLaboratory(id: UUID, laboratoryId: UUID): Result<WorkOrder> =
        safeApiCall { production.sendToLaboratory(id, SendToLaboratoryRequest(laboratoryId)).toDomain() }

    override suspend fun updateStatus(id: UUID, status: String): Result<WorkOrder> =
        safeApiCall {
            // La respuesta solo trae el estado: se vuelve a leer la orden completa.
            production.updateStatus(id, UpdateWorkOrderStatusRequest(status))
            production.getWorkOrder(id).toDomain()
        }

    override suspend fun completeLenses(id: UUID, lensIds: List<UUID>?): Result<WorkOrder> =
        safeApiCall { production.completeLenses(id, CompleteLensesRequest(lensIds)).toDomain() }

    override suspend fun notifyDelay(id: UUID, reason: String, newDate: String): Result<WorkOrder> =
        safeApiCall { production.notifyDelay(id, NotifyDelayRequest(reason, newDate)).toDomain() }

    override suspend fun getTechnicians(): Result<List<NamedRef>> =
        safeApiCall { production.getTechnicians().map { NamedRef(it.id, it.name) } }

    override suspend fun getLaboratories(): Result<List<NamedRef>> =
        safeApiCall { production.getLaboratories().map { NamedRef(it.id, it.name) } }
}
