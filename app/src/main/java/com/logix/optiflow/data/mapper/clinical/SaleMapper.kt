package com.logix.optiflow.data.mapper.clinical

import com.logix.optiflow.data.remote.dto.clinical.SaleDto
import com.logix.optiflow.domain.model.clinical.Sale

fun SaleDto.toDomain(): Sale =
    Sale(
        id = id,
        quotationId = quotationId,
        status = status,
        date = payment?.paidAt ?: closedAt ?: receipt?.issueDate,
        amount = receipt?.totalAmount ?: payment?.amount,
        paymentMethod = payment?.method,
        receiptNumber = receipt?.receiptNumber,
        paymentStatus = payment?.status,
    )
