package com.logix.optiflow.domain.usecase.production

import com.logix.optiflow.domain.model.production.WorkOrder
import com.logix.optiflow.domain.repository.production.WorkOrderRepository
import java.util.UUID
import javax.inject.Inject

class NotifyDeliveryDelayUseCase @Inject constructor(
    private val repository: WorkOrderRepository,
) {
    suspend operator fun invoke(id: UUID, reason: String, newDate: String): Result<WorkOrder> =
        repository.notifyDelay(id, reason, newDate)
}
