package com.logix.optiflow.domain.usecase.inventory

import com.logix.optiflow.domain.model.inventory.StockMovement
import com.logix.optiflow.domain.repository.inventory.InventoryRepository
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

/** Entradas y salidas de una óptica en un periodo, de la más reciente a la más antigua. */
class GetStockMovementsUseCase @Inject constructor(
    private val repository: InventoryRepository,
) {
    suspend operator fun invoke(opticalStoreId: UUID, from: LocalDate, to: LocalDate, sku: String? = null): Result<List<StockMovement>> =
        repository.getMovements(opticalStoreId, from, to, sku)
}
