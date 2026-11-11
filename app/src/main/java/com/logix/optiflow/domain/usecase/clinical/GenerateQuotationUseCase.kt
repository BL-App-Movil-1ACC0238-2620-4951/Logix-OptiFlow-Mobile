package com.logix.optiflow.domain.usecase.clinical

import com.logix.optiflow.domain.model.clinical.Quotation
import com.logix.optiflow.domain.model.clinical.QuotationItem
import com.logix.optiflow.domain.repository.clinical.QuotationRepository
import java.util.UUID
import javax.inject.Inject

class GenerateQuotationUseCase @Inject constructor(
    private val repository: QuotationRepository,
) {
    suspend operator fun invoke(clinicalRecordId: UUID, items: List<QuotationItem>): Result<Quotation> =
        repository.generate(clinicalRecordId, items)
}
