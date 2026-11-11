package com.logix.optiflow.domain.model.clinical

import java.time.Instant
import java.util.UUID

data class Sale(
    val id: UUID,
    val quotationId: UUID?,
    val status: String,
    /** Fecha del pago, del cierre o de la boleta, la primera que exista. */
    val date: Instant?,
    val amount: Double?,
    val paymentMethod: String?,
    val receiptNumber: String?,
    /** APPROVED o REJECTED; un pago rechazado deja la venta pendiente. */
    val paymentStatus: String? = null,
)

/** Estados de `SaleStatus` en el backend. */
object SaleStatus {
    const val PENDING_PAYMENT = "PENDING_PAYMENT"
    const val PAID = "PAID"
    const val CLOSED = "CLOSED"
}
