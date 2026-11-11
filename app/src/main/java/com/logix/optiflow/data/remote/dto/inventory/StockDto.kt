package com.logix.optiflow.data.remote.dto.inventory

import java.util.UUID

data class InventoryDto(
    val id: UUID?,
    val opticalStoreId: UUID?,
    val stocks: List<StockDto>?,
)

data class StockDto(
    val sku: String,
    val name: String?,
    val quantity: Int,
    val minimumStock: Int?,
    val low: Boolean?,
)

data class ReplenishStockRequest(
    val opticalStoreId: UUID,
    val sku: String,
    val supplierId: UUID,
    val quantity: Int,
    val minimumStock: Int?,
)

data class StockMovementDto(
    val id: UUID,
    val createdAt: String?,
    val sku: String,
    val frameModelName: String?,
    val type: String,
    val reason: String?,
    val quantity: Int,
    val saleId: UUID?,
)

data class LowStockAlertDto(
    val sku: String,
    val name: String?,
    val quantity: Int,
    val minimumStock: Int,
)
