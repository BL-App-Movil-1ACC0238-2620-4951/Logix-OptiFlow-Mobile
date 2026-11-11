package com.logix.optiflow.domain.repository.clinical

import com.logix.optiflow.domain.model.clinical.Quotation
import com.logix.optiflow.domain.model.clinical.QuotationItem
import java.util.UUID

interface QuotationRepository {
    suspend fun generate(clinicalRecordId: UUID, items: List<QuotationItem>): Result<Quotation>

    suspend fun get(quotationId: UUID): Result<Quotation>

    suspend fun getByClinicalRecord(clinicalRecordId: UUID): Result<List<Quotation>>

    suspend fun applyDiscount(quotationId: UUID, type: String, value: Double, reason: String?): Result<Quotation>

    /** Con APPROVED el paciente acepta la cotización; con REJECTED hace falta el motivo. */
    suspend fun updateStatus(quotationId: UUID, status: String, reason: String?): Result<Quotation>
}
