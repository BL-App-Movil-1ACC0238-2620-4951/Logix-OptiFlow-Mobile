package com.logix.optiflow.data.repository.clinical

import com.logix.optiflow.data.mapper.clinical.toDomain
import com.logix.optiflow.data.remote.api.clinical.ClinicalApi
import com.logix.optiflow.data.remote.dto.clinical.RecordPaymentRequest
import com.logix.optiflow.data.remote.dto.clinical.RegisterSaleRequest
import com.logix.optiflow.data.remote.dto.clinical.UpdateSaleStatusRequest
import com.logix.optiflow.data.remote.safeApiCall
import com.logix.optiflow.domain.model.clinical.Sale
import com.logix.optiflow.domain.model.clinical.SaleStatus
import com.logix.optiflow.domain.repository.clinical.SaleRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SaleRepositoryImpl @Inject constructor(
    private val api: ClinicalApi,
) : SaleRepository {

    override suspend fun getPatientSales(patientId: UUID): Result<List<Sale>> =
        safeApiCall { api.getPatientSales(patientId).map { it.toDomain() } }

    override suspend fun register(quotationId: UUID): Result<Sale> =
        safeApiCall { api.registerSale(RegisterSaleRequest(quotationId)).toDomain() }

    override suspend fun getSale(saleId: UUID): Result<Sale> =
        safeApiCall { api.getSale(saleId).toDomain() }

    override suspend fun recordCashPayment(saleId: UUID, amount: Double): Result<Sale> =
        safeApiCall { api.recordCashPayment(saleId, RecordPaymentRequest(amount)).toDomain() }

    override suspend fun close(saleId: UUID): Result<Sale> =
        safeApiCall { api.updateSaleStatus(saleId, UpdateSaleStatusRequest(SaleStatus.CLOSED)).toDomain() }

    override suspend fun createPaymentLink(saleId: UUID): Result<String> =
        safeApiCall { api.createPaymentPreference(saleId).initPoint }
}
