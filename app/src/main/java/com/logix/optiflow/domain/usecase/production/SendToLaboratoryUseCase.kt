package com.logix.optiflow.domain.usecase.production

import com.logix.optiflow.domain.model.production.WorkOrder
import com.logix.optiflow.domain.repository.production.WorkOrderRepository
import java.util.UUID
import javax.inject.Inject

class SendToLaboratoryUseCase @Inject constructor(
    private val repository: WorkOrderRepository,
) {
    suspend operator fun invoke(id: UUID, laboratoryId: UUID): Result<WorkOrder> =
        repository.sendToLaboratory(id, laboratoryId)
}
