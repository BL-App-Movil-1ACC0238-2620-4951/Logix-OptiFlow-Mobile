package com.logix.optiflow.data.mapper.clinical

import com.logix.optiflow.data.remote.dto.clinical.QuotationDto
import com.logix.optiflow.data.remote.dto.clinical.QuotationItemRequest
import com.logix.optiflow.domain.model.clinical.Quotation
import com.logix.optiflow.domain.model.clinical.QuotationDiscount
import com.logix.optiflow.domain.model.clinical.QuotationItem

fun QuotationDto.toDomain(): Quotation =
    Quotation(
        id = id,
        clinicalRecordId = clinicalRecordId,
        items =
            items.orEmpty().map {
                QuotationItem(it.itemType, it.productSku, it.description, it.unitPrice, it.quantity)
            },
        discount = discount?.let { QuotationDiscount(it.type, it.value, it.reason, it.amount ?: 0.0) },
        subtotal = subtotal ?: 0.0,
        total = total ?: 0.0,
        currency = currency ?: "PEN",
        status = status,
        rejectionReason = rejectionReason,
    )

fun QuotationItem.toRequest(): QuotationItemRequest =
    QuotationItemRequest(type, sku, description, unitPrice, quantity)
