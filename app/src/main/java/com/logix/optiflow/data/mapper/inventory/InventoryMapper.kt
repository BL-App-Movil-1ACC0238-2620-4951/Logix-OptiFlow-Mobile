package com.logix.optiflow.data.mapper.inventory

import com.logix.optiflow.data.remote.dto.inventory.LowStockAlertDto
import com.logix.optiflow.data.remote.dto.inventory.StockDto
import com.logix.optiflow.data.remote.dto.inventory.StockMovementDto
import com.logix.optiflow.data.remote.dto.inventory.SupplierDto
import com.logix.optiflow.domain.model.inventory.StockItem
import com.logix.optiflow.domain.model.inventory.StockMovement
import com.logix.optiflow.domain.model.inventory.Supplier
import java.time.Instant

fun StockDto.toDomain(): StockItem =
    StockItem(sku = sku, name = name ?: sku, quantity = quantity, minimumStock = minimumStock ?: 0)

fun LowStockAlertDto.toDomain(): StockItem =
    StockItem(sku = sku, name = name ?: sku, quantity = quantity, minimumStock = minimumStock)

fun StockMovementDto.toDomain(): StockMovement =
    StockMovement(
        id = id,
        createdAt = createdAt?.let { runCatching { Instant.parse(it) }.getOrNull() },
        sku = sku,
        name = frameModelName ?: sku,
        isEntry = type == "IN",
        reason = reason,
        quantity = quantity,
    )

fun SupplierDto.toDomain(): Supplier = Supplier(id = id, name = name, contact = contact)
