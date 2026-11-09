package com.logix.optiflow.domain.repository.production

import com.logix.optiflow.domain.model.production.WorkOrder
import java.util.UUID

interface WorkOrderRepository {
    suspend fun getPatientWorkOrders(patientId: UUID): Result<List<WorkOrder>>

    suspend fun getWorkOrders(): Result<List<WorkOrder>>
}
