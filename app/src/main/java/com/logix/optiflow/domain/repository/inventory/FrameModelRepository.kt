package com.logix.optiflow.domain.repository.inventory

import com.logix.optiflow.domain.model.inventory.FrameModel
import java.util.UUID

interface FrameModelRepository {
    suspend fun getFrameCatalog(): Result<List<FrameModel>>

    suspend fun getFrame(sku: String, opticalStoreId: UUID? = null): Result<FrameModel>

    /** Unidades de cada montura (por SKU) en el inventario de una óptica. */
    suspend fun getStoreStock(opticalStoreId: UUID): Result<Map<String, Int>>

    suspend fun addFrame(
        sku: String,
        name: String,
        material: String?,
        shape: String?,
        color: String?,
        price: Double,
    ): Result<FrameModel>

    suspend fun updatePrice(sku: String, price: Double): Result<FrameModel>
}
