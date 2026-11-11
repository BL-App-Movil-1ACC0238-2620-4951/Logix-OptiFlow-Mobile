package com.logix.optiflow.domain.usecase.production

import com.logix.optiflow.domain.model.production.WorkOrder
import com.logix.optiflow.domain.repository.production.WorkOrderRepository
import javax.inject.Inject

/** Órdenes del taller (`GET /work-orders`); todo el personal puede leerlas. */
class GetWorkOrdersUseCase @Inject constructor(
    private val repository: WorkOrderRepository,
) {
    suspend operator fun invoke(): Result<List<WorkOrder>> = repository.getWorkOrders()
}
