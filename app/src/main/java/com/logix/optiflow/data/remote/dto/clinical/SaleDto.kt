package com.logix.optiflow.data.remote.dto.clinical

import java.time.Instant
import java.util.UUID

data class SaleDto(
    val id: UUID,
    val quotationId: UUID?,
    val patientId: UUID?,
    val status: String,
    val closedAt: Instant?,
    val payment: PaymentDto?,
    val receipt: ElectronicReceiptDto?,
)

data class RegisterSaleRequest(
    val quotationId: UUID,
)

data class RecordPaymentRequest(
    val amount: Double,
)

data class UpdateSaleStatusRequest(
    val status: String,
)

data class PaymentPreferenceDto(
    val id: String?,
    val initPoint: String,
)

data class PaymentDto(
    val method: String?,
    val amount: Double?,
    val transactionReference: String?,
    val status: String?,
    val statusDetail: String?,
    val paidAt: Instant?,
)

data class ElectronicReceiptDto(
    val id: UUID?,
    val receiptNumber: String?,
    val issueDate: Instant?,
    val taxAmount: Double?,
    val totalAmount: Double?,
)
