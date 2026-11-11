package com.logix.optiflow.data.remote.api.report

import com.logix.optiflow.data.remote.dto.report.InventoryMetricsDto
import com.logix.optiflow.data.remote.dto.report.ProductionMetricsDto
import com.logix.optiflow.data.remote.dto.report.SalesMetricsDto
import java.util.UUID
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

/** Métricas del admin (`/reports/sales`, `/reports/production`, `/reports/inventory`). */
interface ReportApi {
    @GET("reports/sales")
    suspend fun getSales(@Query("from") from: String, @Query("to") to: String): SalesMetricsDto

    @GET("reports/production")
    suspend fun getProduction(@Query("from") from: String, @Query("to") to: String): ProductionMetricsDto

    @GET("reports/inventory")
    suspend fun getInventory(
        @Query("opticalStoreId") opticalStoreId: UUID,
        @Query("from") from: String,
        @Query("to") to: String,
    ): InventoryMetricsDto

    /** `kind` es `sales`, `production` o `inventory`; el inventario además pide la óptica. */
    @Headers("Accept: text/csv")
    @GET("reports/{kind}")
    suspend fun getCsv(
        @Path("kind") kind: String,
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("opticalStoreId") opticalStoreId: UUID? = null,
    ): ResponseBody
}
