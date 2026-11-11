package com.logix.optiflow.domain.repository.production

import com.logix.optiflow.domain.model.production.NamedRef
import com.logix.optiflow.domain.model.production.WorkOrder
import java.util.UUID

interface WorkOrderRepository {
    suspend fun getPatientWorkOrders(patientId: UUID): Result<List<WorkOrder>>

    suspend fun getWorkOrders(): Result<List<WorkOrder>>

    suspend fun getWorkOrder(id: UUID): Result<WorkOrder>

    suspend fun assignTechnician(id: UUID, technicianId: UUID): Result<WorkOrder>

    /** Una orden pendiente pasa a IN_WORKSHOP al enviarla al laboratorio. */
    suspend fun sendToLaboratory(id: UUID, laboratoryId: UUID): Result<WorkOrder>

    /** Siguiente etapa del Kanban; con DELIVERED registra la entrega. */
    suspend fun updateStatus(id: UUID, status: String): Result<WorkOrder>

    /** Marca como fabricados los lentes indicados, o todos si es null. */
    suspend fun completeLenses(id: UUID, lensIds: List<UUID>?): Result<WorkOrder>

    /** `newDate` en formato ISO (aaaa-mm-dd). */
    suspend fun notifyDelay(id: UUID, reason: String, newDate: String): Result<WorkOrder>

    suspend fun getTechnicians(): Result<List<NamedRef>>

    suspend fun getLaboratories(): Result<List<NamedRef>>
}
