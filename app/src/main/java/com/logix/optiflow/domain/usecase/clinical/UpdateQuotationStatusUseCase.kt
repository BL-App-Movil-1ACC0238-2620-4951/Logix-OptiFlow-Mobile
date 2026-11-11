package com.logix.optiflow.domain.usecase.clinical

import com.logix.optiflow.domain.model.clinical.Quotation
import com.logix.optiflow.domain.repository.clinical.QuotationRepository
import java.util.UUID
import javax.inject.Inject

class UpdateQuotationStatusUseCase @Inject constructor(
    private val repository: QuotationRepository,
) {
    suspend operator fun invoke(quotationId: UUID, status: String, reason: String? = null): Result<Quotation> =
        repository.updateStatus(quotationId, status, reason)
}
