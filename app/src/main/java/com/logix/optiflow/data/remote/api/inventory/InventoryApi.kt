package com.logix.optiflow.data.remote.api.inventory

import com.logix.optiflow.data.remote.dto.inventory.AddFrameModelRequest
import com.logix.optiflow.data.remote.dto.inventory.FrameModelDto
import com.logix.optiflow.data.remote.dto.inventory.InventoryDto
import com.logix.optiflow.data.remote.dto.inventory.LowStockAlertDto
import com.logix.optiflow.data.remote.dto.inventory.RegisterSupplierRequest
import com.logix.optiflow.data.remote.dto.inventory.ReplenishStockRequest
import com.logix.optiflow.data.remote.dto.inventory.StockMovementDto
import com.logix.optiflow.data.remote.dto.inventory.SupplierDto
import com.logix.optiflow.data.remote.dto.inventory.UpdateFramePriceRequest
import java.util.UUID
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface InventoryApi {
    @GET("inventory/frames")
    suspend fun getFrameCatalog(): List<FrameModelDto>

    @GET("inventory/frames/{sku}")
    suspend fun getFrame(
        @Path("sku") sku: String,
        @Query("opticalStoreId") opticalStoreId: UUID? = null,
    ): FrameModelDto

    @POST("inventory/frames")
    suspend fun addFrame(@Body request: AddFrameModelRequest): FrameModelDto

    @PATCH("inventory/frames/{sku}/price")
    suspend fun updateFramePrice(
        @Path("sku") sku: String,
        @Body request: UpdateFramePriceRequest,
    ): FrameModelDto

    @GET("inventory/stock")
    suspend fun getStoreStock(
        @Query("opticalStoreId") opticalStoreId: UUID,
    ): InventoryDto

    @POST("inventory/replenishments")
    suspend fun replenish(@Body request: ReplenishStockRequest): StockMovementDto

    @GET("inventory/movements")
    suspend fun getMovements(
        @Query("opticalStoreId") opticalStoreId: UUID,
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("sku") sku: String? = null,
    ): List<StockMovementDto>

    @GET("inventory/alerts")
    suspend fun getLowStockAlerts(
        @Query("opticalStoreId") opticalStoreId: UUID,
    ): List<LowStockAlertDto>

    @GET("suppliers")
    suspend fun getSuppliers(): List<SupplierDto>

    @POST("suppliers")
    suspend fun registerSupplier(@Body request: RegisterSupplierRequest): SupplierDto
}
