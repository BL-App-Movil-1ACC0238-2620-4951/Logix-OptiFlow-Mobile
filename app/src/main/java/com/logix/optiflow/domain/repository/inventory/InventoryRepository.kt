package com.logix.optiflow.domain.repository.inventory

import com.logix.optiflow.domain.model.inventory.StockItem
import com.logix.optiflow.domain.model.inventory.StockMovement
import com.logix.optiflow.domain.model.inventory.Supplier
import java.time.LocalDate
import java.util.UUID

/** Stock, reposiciones, movimientos y alertas del inventario de una óptica. */
interface InventoryRepository {
    suspend fun getStock(opticalStoreId: UUID): Result<List<StockItem>>

    suspend fun replenish(
        opticalStoreId: UUID,
        sku: String,
        supplierId: UUID,
        quantity: Int,
        minimumStock: Int?,
    ): Result<StockMovement>

    suspend fun getMovements(opticalStoreId: UUID, from: LocalDate, to: LocalDate, sku: String? = null): Result<List<StockMovement>>

    suspend fun getLowStockAlerts(opticalStoreId: UUID): Result<List<StockItem>>

    suspend fun getSuppliers(): Result<List<Supplier>>

    suspend fun registerSupplier(name: String, contact: String?): Result<Supplier>
}
