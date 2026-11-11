package com.logix.optiflow.domain.model.inventory

import java.time.Instant
import java.util.UUID

/** Unidades de una montura en el inventario de una óptica. */
data class StockItem(
    val sku: String,
    val name: String,
    val quantity: Int,
    val minimumStock: Int,
) {
    /** El backend alerta cuando la cantidad llega al mínimo. */
    val low: Boolean get() = quantity <= minimumStock
}

/** Entrada (reposición) o salida (venta) del inventario. */
data class StockMovement(
    val id: UUID,
    val createdAt: Instant?,
    val sku: String,
    val name: String,
    val isEntry: Boolean,
    val reason: String?,
    val quantity: Int,
)

data class Supplier(
    val id: UUID,
    val name: String,
    val contact: String? = null,
)
