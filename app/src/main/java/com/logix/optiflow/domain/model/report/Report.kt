package com.logix.optiflow.domain.model.report

data class SalesMetrics(
    val approvedQuotations: Long,
    val rejectedQuotations: Long,
    val totalAmount: Double,
    val averageTicket: Double,
)

data class ProductionMetrics(
    val ordersByStatus: Map<String, Long>,
    val averageFabricationHours: Double?,
    val delayedOrders: Long,
)

data class InventoryMetrics(
    val unitsIn: Int,
    val unitsOut: Int,
    val lowStockProducts: Long,
)

enum class ReportKind(val path: String) {
    SALES("sales"),
    PRODUCTION("production"),
    INVENTORY("inventory"),
}
