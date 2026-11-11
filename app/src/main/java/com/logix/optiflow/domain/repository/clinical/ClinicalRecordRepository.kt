package com.logix.optiflow.domain.repository.clinical

import com.logix.optiflow.domain.model.clinical.ClinicalRecord
import com.logix.optiflow.domain.model.clinical.MedicalHistory
import com.logix.optiflow.domain.model.clinical.OpticalPrescription
import java.util.UUID

interface ClinicalRecordRepository {
    suspend fun getPatientRecords(patientId: UUID): Result<List<ClinicalRecord>>

    /** Cantidad de pacientes que tienen al menos una ficha clínica. */
    suspend fun countPatientsWithRecords(): Result<Long>

    suspend fun getPrescription(clinicalRecordId: UUID): Result<OpticalPrescription>

    /** Registra el examen de la cita: la ficha pasa de PENDING a OPEN. */
    suspend fun registerExamination(patientId: UUID, appointmentId: UUID, observations: String?): Result<ClinicalRecord>

    suspend fun recordMedicalHistory(clinicalRecordId: UUID, history: MedicalHistory): Result<ClinicalRecord>

    suspend fun generatePrescription(clinicalRecordId: UUID, prescription: OpticalPrescription): Result<OpticalPrescription>
}
