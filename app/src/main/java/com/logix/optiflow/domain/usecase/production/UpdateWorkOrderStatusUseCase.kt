package com.logix.optiflow.domain.usecase.production

import com.logix.optiflow.domain.model.production.WorkOrder
import com.logix.optiflow.domain.repository.production.WorkOrderRepository
import java.util.UUID
import javax.inject.Inject

class UpdateWorkOrderStatusUseCase @Inject constructor(
    private val repository: WorkOrderRepository,
) {
    suspend operator fun invoke(id: UUID, status: String): Result<WorkOrder> =
        repository.updateStatus(id, status)
}
