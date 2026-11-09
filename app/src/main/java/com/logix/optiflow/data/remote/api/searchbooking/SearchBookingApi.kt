package com.logix.optiflow.data.remote.api.searchbooking

import com.logix.optiflow.data.remote.dto.clinical.ClinicalRecordDto
import com.logix.optiflow.data.remote.dto.clinical.ClinicalRecordsSummaryDto
import com.logix.optiflow.data.remote.dto.production.WorkOrderDto
import com.logix.optiflow.data.remote.dto.searchbooking.AppointmentDto
import com.logix.optiflow.data.remote.dto.searchbooking.BookAppointmentRequest
import com.logix.optiflow.data.remote.dto.searchbooking.LoginRequest
import com.logix.optiflow.data.remote.dto.searchbooking.LoginResponse
import com.logix.optiflow.data.remote.dto.searchbooking.OpticalStoreDto
import com.logix.optiflow.data.remote.dto.searchbooking.PatientDto
import com.logix.optiflow.data.remote.dto.searchbooking.RegisterPatientRequest
import com.logix.optiflow.data.remote.dto.searchbooking.StaffMemberDto
import com.logix.optiflow.data.remote.dto.searchbooking.TimeSlotDto
import com.logix.optiflow.data.remote.dto.searchbooking.UpdateAppointmentStatusRequest
import java.util.UUID
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SearchBookingApi {

    @GET("optical-stores")
    suspend fun listOpticalStores(
        @Query("name") name: String? = null,
        @Query("address") address: String? = null,
        @Query("minRating") minRating: Double? = null,
    ): List<OpticalStoreDto>

    @GET("optical-stores/{id}/time-slots")
    suspend fun getTimeSlots(
        @Path("id") opticalStoreId: UUID,
    ): List<TimeSlotDto>

    @POST("patients")
    suspend fun registerPatient(
        @Body request: RegisterPatientRequest,
    ): PatientDto

    @POST("auth/tokens")
    suspend fun login(
        @Body request: LoginRequest,
    ): LoginResponse

    @GET("patients")
    suspend fun searchPatients(
        @Query("name") name: String? = null,
        @Query("email") email: String? = null,
    ): List<PatientDto>

    @GET("patients/{patientId}")
    suspend fun getPatient(
        @Path("patientId") patientId: UUID,
    ): PatientDto

    @POST("appointments")
    suspend fun bookAppointment(
        @Body request: BookAppointmentRequest,
    ): AppointmentDto

    @GET("appointments")
    suspend fun getStoreAgenda(
        @Query("opticalStoreId") opticalStoreId: UUID,
        @Query("date") date: String,
    ): List<AppointmentDto>

    @PATCH("appointments/{id}/status")
    suspend fun updateAppointmentStatus(
        @Path("id") appointmentId: UUID,
        @Body request: UpdateAppointmentStatusRequest,
    ): AppointmentDto

    @GET("staff-members/{id}")
    suspend fun getStaffMember(
        @Path("id") staffMemberId: UUID,
    ): StaffMemberDto

    @GET("patients/{patientId}/appointments")
    suspend fun getPatientAppointments(
        @Path("patientId") patientId: UUID,
    ): List<AppointmentDto>

    @GET("patients/{patientId}/work-orders")
    suspend fun getPatientWorkOrders(
        @Path("patientId") patientId: UUID,
    ): List<WorkOrderDto>

    @GET("work-orders")
    suspend fun getWorkOrders(
        @Query("status") status: String? = null,
    ): List<WorkOrderDto>

    @GET("clinical-records/summary")
    suspend fun getClinicalRecordsSummary(): ClinicalRecordsSummaryDto

    @GET("patients/{patientId}/clinical-records")
    suspend fun getPatientClinicalRecords(
        @Path("patientId") patientId: UUID,
    ): List<ClinicalRecordDto>
}
