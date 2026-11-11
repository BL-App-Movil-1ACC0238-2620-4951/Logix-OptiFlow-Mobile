package com.logix.optiflow.domain.usecase.report

import com.logix.optiflow.domain.model.report.InventoryMetrics
import com.logix.optiflow.domain.model.report.ProductionMetrics
import com.logix.optiflow.domain.model.report.SalesMetrics
import com.logix.optiflow.domain.repository.report.ReportRepository
import java.time.YearMonth
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/** Ventas, producción e inventario de un mes, y las ventas de los 6 meses que terminan en él. */
data class MonthlyReport(
    val month: YearMonth,
    val sales: SalesMetrics,
    val production: ProductionMetrics?,
    val inventory: InventoryMetrics?,
    /** Total vendido por mes, del más antiguo al pedido. */
    val salesHistory: List<Pair<YearMonth, Double>>,
)

class GetMonthlyReportUseCase @Inject constructor(
    private val repository: ReportRepository,
) {
    suspend operator fun invoke(month: YearMonth, opticalStoreId: UUID?): Result<MonthlyReport> =
        runCatching {
            coroutineScope {
                val from = month.atDay(1)
                val to = month.atEndOfMonth()
                val production = async { repository.getProduction(from, to).getOrNull() }
                val inventory = async { opticalStoreId?.let { repository.getInventory(it, from, to).getOrNull() } }
                val history =
                    (5 downTo 1).map { back ->
                        val m = month.minusMonths(back.toLong())
                        async { m to (repository.getSales(m.atDay(1), m.atEndOfMonth()).getOrNull()?.totalAmount ?: 0.0) }
                    }
                val sales = repository.getSales(from, to).getOrThrow()
                MonthlyReport(month, sales, production.await(), inventory.await(), history.awaitAll() + (month to sales.totalAmount))
            }
        }
}
