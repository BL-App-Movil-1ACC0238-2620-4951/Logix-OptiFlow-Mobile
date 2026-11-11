package com.logix.optiflow.domain.usecase.clinical

import com.logix.optiflow.domain.model.clinical.Quotation
import com.logix.optiflow.domain.repository.clinical.QuotationRepository
import java.util.UUID
import javax.inject.Inject

class GetQuotationUseCase @Inject constructor(
    private val repository: QuotationRepository,
) {
    suspend operator fun invoke(quotationId: UUID): Result<Quotation> =
        repository.get(quotationId)
}
