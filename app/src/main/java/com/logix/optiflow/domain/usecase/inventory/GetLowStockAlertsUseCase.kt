package com.logix.optiflow.domain.usecase.inventory

import com.logix.optiflow.domain.model.inventory.StockItem
import com.logix.optiflow.domain.repository.inventory.InventoryRepository
import java.util.UUID
import javax.inject.Inject

/** Monturas de una óptica que llegaron a su stock mínimo. */
class GetLowStockAlertsUseCase @Inject constructor(
    private val repository: InventoryRepository,
) {
    suspend operator fun invoke(opticalStoreId: UUID): Result<List<StockItem>> = repository.getLowStockAlerts(opticalStoreId)
}
