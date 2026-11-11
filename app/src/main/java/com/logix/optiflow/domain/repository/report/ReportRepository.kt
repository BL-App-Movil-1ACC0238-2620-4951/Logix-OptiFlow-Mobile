package com.logix.optiflow.domain.repository.report

import com.logix.optiflow.domain.model.report.InventoryMetrics
import com.logix.optiflow.domain.model.report.ProductionMetrics
import com.logix.optiflow.domain.model.report.ReportKind
import com.logix.optiflow.domain.model.report.SalesMetrics
import java.time.LocalDate
import java.util.UUID

interface ReportRepository {
    suspend fun getSales(from: LocalDate, to: LocalDate): Result<SalesMetrics>

    suspend fun getProduction(from: LocalDate, to: LocalDate): Result<ProductionMetrics>

    suspend fun getInventory(opticalStoreId: UUID, from: LocalDate, to: LocalDate): Result<InventoryMetrics>

    /** Contenido CSV del reporte. */
    suspend fun getCsv(kind: ReportKind, from: LocalDate, to: LocalDate, opticalStoreId: UUID?): Result<String>
}
