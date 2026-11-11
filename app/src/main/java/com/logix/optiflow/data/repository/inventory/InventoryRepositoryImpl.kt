package com.logix.optiflow.data.repository.inventory

import com.logix.optiflow.data.mapper.inventory.toDomain
import com.logix.optiflow.data.remote.api.inventory.InventoryApi
import com.logix.optiflow.data.remote.dto.inventory.RegisterSupplierRequest
import com.logix.optiflow.data.remote.dto.inventory.ReplenishStockRequest
import com.logix.optiflow.data.remote.safeApiCall
import com.logix.optiflow.domain.model.inventory.StockItem
import com.logix.optiflow.domain.model.inventory.StockMovement
import com.logix.optiflow.domain.model.inventory.Supplier
import com.logix.optiflow.domain.repository.inventory.InventoryRepository
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventoryRepositoryImpl @Inject constructor(
    private val api: InventoryApi,
) : InventoryRepository {

    override suspend fun getStock(opticalStoreId: UUID): Result<List<StockItem>> =
        safeApiCall { api.getStoreStock(opticalStoreId).stocks.orEmpty().map { it.toDomain() } }

    override suspend fun replenish(
        opticalStoreId: UUID,
        sku: String,
        supplierId: UUID,
        quantity: Int,
        minimumStock: Int?,
    ): Result<StockMovement> =
        safeApiCall {
            api.replenish(ReplenishStockRequest(opticalStoreId, sku, supplierId, quantity, minimumStock)).toDomain()
        }

    override suspend fun getMovements(
        opticalStoreId: UUID,
        from: LocalDate,
        to: LocalDate,
        sku: String?,
    ): Result<List<StockMovement>> =
        safeApiCall { api.getMovements(opticalStoreId, from.toString(), to.toString(), sku).map { it.toDomain() } }

    override suspend fun getLowStockAlerts(opticalStoreId: UUID): Result<List<StockItem>> =
        safeApiCall { api.getLowStockAlerts(opticalStoreId).map { it.toDomain() } }

    override suspend fun getSuppliers(): Result<List<Supplier>> =
        safeApiCall { api.getSuppliers().map { it.toDomain() } }

    override suspend fun registerSupplier(name: String, contact: String?): Result<Supplier> =
        safeApiCall { api.registerSupplier(RegisterSupplierRequest(name, contact)).toDomain() }
}
