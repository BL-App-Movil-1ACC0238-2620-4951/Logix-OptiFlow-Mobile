package com.logix.optiflow.domain.usecase.clinical

import com.logix.optiflow.domain.model.clinical.Sale
import com.logix.optiflow.domain.repository.clinical.SaleRepository
import java.util.UUID
import javax.inject.Inject

class GetPatientSalesUseCase @Inject constructor(
    private val repository: SaleRepository,
) {
    /** Compras del paciente en el orden del backend: primero las pendientes de pago. */
    suspend operator fun invoke(patientId: UUID): Result<List<Sale>> =
        repository.getPatientSales(patientId)
}
