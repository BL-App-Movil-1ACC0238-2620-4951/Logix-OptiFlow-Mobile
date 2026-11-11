package com.logix.optiflow.domain.usecase.clinical

import com.logix.optiflow.domain.model.clinical.OpticalPrescription
import com.logix.optiflow.domain.repository.clinical.ClinicalRecordRepository
import java.util.UUID
import javax.inject.Inject

class GetPrescriptionUseCase @Inject constructor(
    private val repository: ClinicalRecordRepository,
) {
    suspend operator fun invoke(clinicalRecordId: UUID): Result<OpticalPrescription> =
        repository.getPrescription(clinicalRecordId)
}
