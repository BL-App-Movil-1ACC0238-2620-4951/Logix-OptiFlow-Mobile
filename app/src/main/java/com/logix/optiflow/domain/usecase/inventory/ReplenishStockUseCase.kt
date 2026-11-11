package com.logix.optiflow.domain.usecase.inventory

import com.logix.optiflow.domain.model.inventory.StockMovement
import com.logix.optiflow.domain.repository.inventory.InventoryRepository
import java.util.UUID
import javax.inject.Inject

/** Ingresa al stock las unidades que entregó un proveedor. */
class ReplenishStockUseCase @Inject constructor(
    private val repository: InventoryRepository,
) {
    suspend operator fun invoke(
        opticalStoreId: UUID,
        sku: String,
        supplierId: UUID,
        quantity: Int,
        minimumStock: Int?,
    ): Result<StockMovement> = repository.replenish(opticalStoreId, sku, supplierId, quantity, minimumStock)
}
