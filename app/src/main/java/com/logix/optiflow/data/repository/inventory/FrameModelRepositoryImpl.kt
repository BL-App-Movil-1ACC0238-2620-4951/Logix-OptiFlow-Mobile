package com.logix.optiflow.data.repository.inventory

import com.logix.optiflow.data.mapper.inventory.toDomain
import com.logix.optiflow.data.remote.api.inventory.InventoryApi
import com.logix.optiflow.data.remote.dto.inventory.AddFrameModelRequest
import com.logix.optiflow.data.remote.dto.inventory.UpdateFramePriceRequest
import com.logix.optiflow.data.remote.safeApiCall
import com.logix.optiflow.domain.model.inventory.FrameModel
import com.logix.optiflow.domain.repository.inventory.FrameModelRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FrameModelRepositoryImpl @Inject constructor(
    private val api: InventoryApi,
) : FrameModelRepository {

    override suspend fun getFrameCatalog(): Result<List<FrameModel>> =
        safeApiCall { api.getFrameCatalog().map { it.toDomain() } }

    override suspend fun getFrame(sku: String, opticalStoreId: UUID?): Result<FrameModel> =
        safeApiCall { api.getFrame(sku, opticalStoreId).toDomain() }

    override suspend fun getStoreStock(opticalStoreId: UUID): Result<Map<String, Int>> =
        safeApiCall { api.getStoreStock(opticalStoreId).stocks.orEmpty().associate { it.sku to it.quantity } }

    override suspend fun addFrame(
        sku: String,
        name: String,
        material: String?,
        shape: String?,
        color: String?,
        price: Double,
    ): Result<FrameModel> =
        safeApiCall { api.addFrame(AddFrameModelRequest(sku, name, material, shape, color, price)).toDomain() }

    override suspend fun updatePrice(sku: String, price: Double): Result<FrameModel> =
        safeApiCall { api.updateFramePrice(sku, UpdateFramePriceRequest(price)).toDomain() }
}
