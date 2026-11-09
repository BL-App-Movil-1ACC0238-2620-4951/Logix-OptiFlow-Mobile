package com.logix.optiflow.data.repository.clinical

import com.logix.optiflow.data.mapper.clinical.toDomain
import com.logix.optiflow.data.remote.api.searchbooking.SearchBookingApi
import com.logix.optiflow.data.remote.safeApiCall
import com.logix.optiflow.domain.model.clinical.ClinicalRecord
import com.logix.optiflow.domain.repository.clinical.ClinicalRecordRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClinicalRecordRepositoryImpl @Inject constructor(
    private val api: SearchBookingApi,
) : ClinicalRecordRepository {

    override suspend fun getPatientRecords(patientId: UUID): Result<List<ClinicalRecord>> =
        safeApiCall { api.getPatientClinicalRecords(patientId).map { it.toDomain() } }

    override suspend fun countPatientsWithRecords(): Result<Long> =
        safeApiCall { api.getClinicalRecordsSummary().patientsWithRecords }
}
