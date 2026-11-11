package com.logix.optiflow.domain.usecase.clinical

import com.logix.optiflow.domain.model.clinical.Sale
import com.logix.optiflow.domain.repository.clinical.SaleRepository
import java.util.UUID
import javax.inject.Inject

class CloseSaleUseCase @Inject constructor(
    private val repository: SaleRepository,
) {
    suspend operator fun invoke(saleId: UUID): Result<Sale> =
        repository.close(saleId)
}
