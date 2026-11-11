package com.logix.optiflow.data.repository.clinical

import com.logix.optiflow.data.mapper.clinical.toDomain
import com.logix.optiflow.data.remote.api.clinical.ClinicalApi
import com.logix.optiflow.data.remote.api.searchbooking.SearchBookingApi
import com.logix.optiflow.data.remote.dto.clinical.ClinicalRecordDto
import com.logix.optiflow.data.remote.dto.clinical.GenerateOpticalPrescriptionRequest
import com.logix.optiflow.data.remote.dto.clinical.RecordMedicalHistoryRequest
import com.logix.optiflow.data.remote.dto.clinical.RegisterClinicalRecordRequest
import com.logix.optiflow.data.remote.safeApiCall
import com.logix.optiflow.data.repository.offline.OfflineDataRepositoryImpl
import com.logix.optiflow.domain.model.clinical.ClinicalRecord
import com.logix.optiflow.domain.model.clinical.MedicalHistory
import com.logix.optiflow.domain.model.clinical.OpticalPrescription
import com.logix.optiflow.domain.repository.clinical.ClinicalRecordRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClinicalRecordRepositoryImpl @Inject constructor(
    private val api: SearchBookingApi,
    private val clinicalApi: ClinicalApi,
    private val offline: OfflineDataRepositoryImpl,
) : ClinicalRecordRepository {

    override suspend fun getPatientRecords(patientId: UUID): Result<List<ClinicalRecord>> =
        offline.list("clinical-records/$patientId", ClinicalRecordDto::class.java) { api.getPatientClinicalRecords(patientId) }
            .map { dtos -> dtos.map { it.toDomain() } }

    override suspend fun countPatientsWithRecords(): Result<Long> =
        safeApiCall { api.getClinicalRecordsSummary().patientsWithRecords }

    override suspend fun getPrescription(clinicalRecordId: UUID): Result<OpticalPrescription> =
        safeApiCall { clinicalApi.getPrescription(clinicalRecordId).toDomain() }

    override suspend fun registerExamination(
        patientId: UUID,
        appointmentId: UUID,
        observations: String?,
    ): Result<ClinicalRecord> =
        safeApiCall {
            clinicalApi.registerClinicalRecord(
                RegisterClinicalRecordRequest(patientId, appointmentId, null, observations),
            ).toDomain()
        }

    override suspend fun recordMedicalHistory(
        clinicalRecordId: UUID,
        history: MedicalHistory,
    ): Result<ClinicalRecord> =
        safeApiCall {
            clinicalApi.recordMedicalHistory(
                clinicalRecordId,
                RecordMedicalHistoryRequest(
                    history.allergies,
                    history.previousConditions,
                    history.familyOcularHistory,
                ),
            ).toDomain()
        }

    override suspend fun generatePrescription(
        clinicalRecordId: UUID,
        prescription: OpticalPrescription,
    ): Result<OpticalPrescription> =
        safeApiCall {
            clinicalApi.generatePrescription(
                clinicalRecordId,
                GenerateOpticalPrescriptionRequest(
                    sphereOD = requireNotNull(prescription.sphereOD),
                    cylinderOD = requireNotNull(prescription.cylinderOD),
                    axisOD = requireNotNull(prescription.axisOD),
                    sphereOS = requireNotNull(prescription.sphereOS),
                    cylinderOS = requireNotNull(prescription.cylinderOS),
                    axisOS = requireNotNull(prescription.axisOS),
                    addition = prescription.addition,
                    treatment = prescription.treatment,
                    recommendedFrameType = prescription.recommendedFrameType,
                ),
            ).toDomain()
        }
}
