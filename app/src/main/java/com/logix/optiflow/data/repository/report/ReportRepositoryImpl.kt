package com.logix.optiflow.data.repository.report

import com.logix.optiflow.data.remote.api.report.ReportApi
import com.logix.optiflow.data.remote.safeApiCall
import com.logix.optiflow.domain.model.report.InventoryMetrics
import com.logix.optiflow.domain.model.report.ProductionMetrics
import com.logix.optiflow.domain.model.report.ReportKind
import com.logix.optiflow.domain.model.report.SalesMetrics
import com.logix.optiflow.domain.repository.report.ReportRepository
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepositoryImpl @Inject constructor(
    private val api: ReportApi,
) : ReportRepository {

    override suspend fun getSales(from: LocalDate, to: LocalDate): Result<SalesMetrics> =
        safeApiCall {
            api.getSales(from.toString(), to.toString()).let {
                SalesMetrics(it.approvedQuotations, it.rejectedQuotations, it.totalAmount ?: 0.0, it.averageTicket ?: 0.0)
            }
        }

    override suspend fun getProduction(from: LocalDate, to: LocalDate): Result<ProductionMetrics> =
        safeApiCall {
            api.getProduction(from.toString(), to.toString()).let {
                ProductionMetrics(it.ordersByStatus.orEmpty(), it.averageFabricationHours, it.delayedOrders)
            }
        }

    override suspend fun getInventory(opticalStoreId: UUID, from: LocalDate, to: LocalDate): Result<InventoryMetrics> =
        safeApiCall {
            api.getInventory(opticalStoreId, from.toString(), to.toString()).let {
                InventoryMetrics(it.unitsIn, it.unitsOut, it.lowStockProducts)
            }
        }

    override suspend fun getCsv(kind: ReportKind, from: LocalDate, to: LocalDate, opticalStoreId: UUID?): Result<String> =
        safeApiCall {
            val store = if (kind == ReportKind.INVENTORY) opticalStoreId else null
            api.getCsv(kind.path, from.toString(), to.toString(), store).use { it.string() }
        }
}
