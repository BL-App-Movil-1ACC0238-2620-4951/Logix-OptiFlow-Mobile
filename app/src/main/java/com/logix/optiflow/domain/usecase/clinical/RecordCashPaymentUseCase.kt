package com.logix.optiflow.domain.usecase.clinical

import com.logix.optiflow.domain.model.clinical.Sale
import com.logix.optiflow.domain.repository.clinical.SaleRepository
import java.util.UUID
import javax.inject.Inject

class RecordCashPaymentUseCase @Inject constructor(
    private val repository: SaleRepository,
) {
    suspend operator fun invoke(saleId: UUID, amount: Double): Result<Sale> =
        repository.recordCashPayment(saleId, amount)
}
