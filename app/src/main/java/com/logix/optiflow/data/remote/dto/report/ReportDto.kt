package com.logix.optiflow.data.remote.dto.report

data class SalesMetricsDto(
    val approvedQuotations: Long,
    val rejectedQuotations: Long,
    val totalAmount: Double?,
    val averageTicket: Double?,
)

data class ProductionMetricsDto(
    val ordersByStatus: Map<String, Long>?,
    val averageFabricationHours: Double?,
    val delayedOrders: Long,
)

data class InventoryMetricsDto(
    val unitsIn: Int,
    val unitsOut: Int,
    val lowStockProducts: Long,
)
