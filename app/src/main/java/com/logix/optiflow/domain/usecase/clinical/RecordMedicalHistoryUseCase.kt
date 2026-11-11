package com.logix.optiflow.domain.usecase.clinical

import com.logix.optiflow.domain.model.clinical.ClinicalRecord
import com.logix.optiflow.domain.model.clinical.MedicalHistory
import com.logix.optiflow.domain.repository.clinical.ClinicalRecordRepository
import java.util.UUID
import javax.inject.Inject

class RecordMedicalHistoryUseCase @Inject constructor(
    private val repository: ClinicalRecordRepository,
) {
    suspend operator fun invoke(clinicalRecordId: UUID, history: MedicalHistory): Result<ClinicalRecord> =
        repository.recordMedicalHistory(clinicalRecordId, history)
}
