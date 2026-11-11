package com.logix.optiflow.data.remote.api.clinical

import com.logix.optiflow.data.remote.dto.clinical.ApplyDiscountRequest
import com.logix.optiflow.data.remote.dto.clinical.ClinicalRecordDto
import com.logix.optiflow.data.remote.dto.clinical.GenerateOpticalPrescriptionRequest
import com.logix.optiflow.data.remote.dto.clinical.GenerateQuotationRequest
import com.logix.optiflow.data.remote.dto.clinical.OpticalPrescriptionDto
import com.logix.optiflow.data.remote.dto.clinical.PaymentPreferenceDto
import com.logix.optiflow.data.remote.dto.clinical.QuotationDto
import com.logix.optiflow.data.remote.dto.clinical.RecordMedicalHistoryRequest
import com.logix.optiflow.data.remote.dto.clinical.RecordPaymentRequest
import com.logix.optiflow.data.remote.dto.clinical.RegisterClinicalRecordRequest
import com.logix.optiflow.data.remote.dto.clinical.RegisterSaleRequest
import com.logix.optiflow.data.remote.dto.clinical.SaleDto
import com.logix.optiflow.data.remote.dto.clinical.UpdateQuotationStatusRequest
import com.logix.optiflow.data.remote.dto.clinical.UpdateSaleStatusRequest
import java.util.UUID
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ClinicalApi {

    @GET("patients/{patientId}/sales")
    suspend fun getPatientSales(
        @Path("patientId") patientId: UUID,
    ): List<SaleDto>

    @GET("clinical-records/{id}/prescription")
    suspend fun getPrescription(
        @Path("id") clinicalRecordId: UUID,
    ): OpticalPrescriptionDto

    @POST("clinical-records")
    suspend fun registerClinicalRecord(
        @Body request: RegisterClinicalRecordRequest,
    ): ClinicalRecordDto

    @PUT("clinical-records/{id}/medical-history")
    suspend fun recordMedicalHistory(
        @Path("id") clinicalRecordId: UUID,
        @Body request: RecordMedicalHistoryRequest,
    ): ClinicalRecordDto

    @POST("clinical-records/{id}/prescription")
    suspend fun generatePrescription(
        @Path("id") clinicalRecordId: UUID,
        @Body request: GenerateOpticalPrescriptionRequest,
    ): OpticalPrescriptionDto

    @POST("quotations")
    suspend fun generateQuotation(
        @Body request: GenerateQuotationRequest,
    ): QuotationDto

    @GET("clinical-records/{id}/quotations")
    suspend fun getRecordQuotations(
        @Path("id") clinicalRecordId: UUID,
    ): List<QuotationDto>

    @PATCH("quotations/{id}/discount")
    suspend fun applyQuotationDiscount(
        @Path("id") quotationId: UUID,
        @Body request: ApplyDiscountRequest,
    ): QuotationDto

    @PATCH("quotations/{id}/status")
    suspend fun updateQuotationStatus(
        @Path("id") quotationId: UUID,
        @Body request: UpdateQuotationStatusRequest,
    ): QuotationDto

    @GET("quotations/{id}")
    suspend fun getQuotation(
        @Path("id") quotationId: UUID,
    ): QuotationDto

    @POST("sales")
    suspend fun registerSale(
        @Body request: RegisterSaleRequest,
    ): SaleDto

    @GET("sales/{id}")
    suspend fun getSale(
        @Path("id") saleId: UUID,
    ): SaleDto

    @POST("sales/{id}/payments")
    suspend fun recordCashPayment(
        @Path("id") saleId: UUID,
        @Body request: RecordPaymentRequest,
    ): SaleDto

    @PATCH("sales/{id}/status")
    suspend fun updateSaleStatus(
        @Path("id") saleId: UUID,
        @Body request: UpdateSaleStatusRequest,
    ): SaleDto

    @POST("sales/{id}/payment-preference")
    suspend fun createPaymentPreference(
        @Path("id") saleId: UUID,
    ): PaymentPreferenceDto
}
