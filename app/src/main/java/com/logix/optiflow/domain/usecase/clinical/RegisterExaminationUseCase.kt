package com.logix.optiflow.domain.usecase.clinical

import com.logix.optiflow.domain.model.clinical.ClinicalRecord
import com.logix.optiflow.domain.repository.clinical.ClinicalRecordRepository
import java.util.UUID
import javax.inject.Inject

class RegisterExaminationUseCase @Inject constructor(
    private val repository: ClinicalRecordRepository,
) {
    suspend operator fun invoke(patientId: UUID, appointmentId: UUID, observations: String): Result<ClinicalRecord> =
        repository.registerExamination(patientId, appointmentId, observations.trim().ifBlank { null })
}
