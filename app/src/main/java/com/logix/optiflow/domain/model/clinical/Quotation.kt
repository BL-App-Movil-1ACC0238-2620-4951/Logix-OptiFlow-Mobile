package com.logix.optiflow.domain.model.clinical

import java.util.UUID

/** Estados de `QuotationStatus` en el backend. */
object QuotationStatus {
    const val DRAFT = "DRAFT"
    const val APPROVED = "APPROVED"
    const val REJECTED = "REJECTED"
}

data class Quotation(
    val id: UUID,
    val clinicalRecordId: UUID?,
    val items: List<QuotationItem>,
    val discount: QuotationDiscount?,
    val subtotal: Double,
    val total: Double,
    val currency: String,
    val status: String,
    val rejectionReason: String?,
)

/** `type` es FRAME, LENS, TREATMENT o SERVICE, como en el backend. */
data class QuotationItem(
    val type: String,
    val sku: String?,
    val description: String,
    val unitPrice: Double,
    val quantity: Int = 1,
)

/** `type` es PERCENTAGE o FIXED_AMOUNT; `amount` es lo que se descuenta en soles. */
data class QuotationDiscount(
    val type: String,
    val value: Double,
    val reason: String?,
    val amount: Double,
)
