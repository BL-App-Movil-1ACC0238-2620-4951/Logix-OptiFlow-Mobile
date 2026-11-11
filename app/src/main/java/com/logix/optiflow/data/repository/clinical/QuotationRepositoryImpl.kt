package com.logix.optiflow.data.repository.clinical

import com.logix.optiflow.data.mapper.clinical.toDomain
import com.logix.optiflow.data.mapper.clinical.toRequest
import com.logix.optiflow.data.remote.api.clinical.ClinicalApi
import com.logix.optiflow.data.remote.dto.clinical.ApplyDiscountRequest
import com.logix.optiflow.data.remote.dto.clinical.GenerateQuotationRequest
import com.logix.optiflow.data.remote.dto.clinical.UpdateQuotationStatusRequest
import com.logix.optiflow.data.remote.safeApiCall
import com.logix.optiflow.domain.model.clinical.Quotation
import com.logix.optiflow.domain.model.clinical.QuotationItem
import com.logix.optiflow.domain.repository.clinical.QuotationRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuotationRepositoryImpl @Inject constructor(
    private val api: ClinicalApi,
) : QuotationRepository {

    override suspend fun generate(clinicalRecordId: UUID, items: List<QuotationItem>): Result<Quotation> =
        safeApiCall {
            api.generateQuotation(GenerateQuotationRequest(clinicalRecordId, items.map { it.toRequest() })).toDomain()
        }

    override suspend fun get(quotationId: UUID): Result<Quotation> =
        safeApiCall { api.getQuotation(quotationId).toDomain() }

    override suspend fun getByClinicalRecord(clinicalRecordId: UUID): Result<List<Quotation>> =
        safeApiCall { api.getRecordQuotations(clinicalRecordId).map { it.toDomain() } }

    override suspend fun applyDiscount(quotationId: UUID, type: String, value: Double, reason: String?): Result<Quotation> =
        safeApiCall { api.applyQuotationDiscount(quotationId, ApplyDiscountRequest(type, value, reason)).toDomain() }

    override suspend fun updateStatus(quotationId: UUID, status: String, reason: String?): Result<Quotation> =
        safeApiCall { api.updateQuotationStatus(quotationId, UpdateQuotationStatusRequest(status, reason)).toDomain() }
}
