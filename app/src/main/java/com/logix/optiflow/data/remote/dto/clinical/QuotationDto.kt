package com.logix.optiflow.data.remote.dto.clinical

import java.util.UUID

data class QuotationDto(
    val id: UUID,
    val clinicalRecordId: UUID?,
    val items: List<QuotationItemDto>?,
    val discount: DiscountDto?,
    val subtotal: Double?,
    val total: Double?,
    val currency: String?,
    val status: String,
    val rejectionReason: String?,
)

data class QuotationItemDto(
    val id: UUID?,
    val itemType: String,
    val productSku: String?,
    val description: String,
    val unitPrice: Double,
    val quantity: Int,
    val subtotal: Double?,
)

data class DiscountDto(
    val type: String,
    val value: Double,
    val reason: String?,
    val amount: Double?,
)

data class GenerateQuotationRequest(
    val clinicalRecordId: UUID,
    val items: List<QuotationItemRequest>,
)

data class QuotationItemRequest(
    val itemType: String,
    val productSku: String?,
    val description: String,
    val unitPrice: Double,
    val quantity: Int,
)

data class ApplyDiscountRequest(
    val type: String,
    val value: Double,
    val reason: String?,
)

data class UpdateQuotationStatusRequest(
    val status: String,
    val reason: String? = null,
)
