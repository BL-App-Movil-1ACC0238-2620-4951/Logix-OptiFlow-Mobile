package com.logix.optiflow.domain.usecase.clinical

import com.logix.optiflow.domain.model.clinical.Quotation
import com.logix.optiflow.domain.repository.clinical.QuotationRepository
import java.util.UUID
import javax.inject.Inject

class ApplyQuotationDiscountUseCase @Inject constructor(
    private val repository: QuotationRepository,
) {
    suspend operator fun invoke(quotationId: UUID, type: String, value: Double, reason: String?): Result<Quotation> =
        repository.applyDiscount(quotationId, type, value, reason)
}
