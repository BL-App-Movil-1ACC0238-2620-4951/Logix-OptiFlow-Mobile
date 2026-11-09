package com.logix.optiflow.domain.repository.clinical

import com.logix.optiflow.domain.model.clinical.ClinicalRecord
import java.util.UUID

interface ClinicalRecordRepository {
    suspend fun getPatientRecords(patientId: UUID): Result<List<ClinicalRecord>>

    /** Cantidad de pacientes que tienen al menos una ficha clínica. */
    suspend fun countPatientsWithRecords(): Result<Long>
}
