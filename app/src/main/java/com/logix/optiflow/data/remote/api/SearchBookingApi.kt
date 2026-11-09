package com.logix.optiflow.data.remote.api

import com.logix.optiflow.data.remote.dto.AppointmentDto
import com.logix.optiflow.data.remote.dto.BookAppointmentRequest
import com.logix.optiflow.data.remote.dto.LoginRequest
import com.logix.optiflow.data.remote.dto.LoginResponse
import com.logix.optiflow.data.remote.dto.OpticalStoreDto
import com.logix.optiflow.data.remote.dto.PatientDto
import com.logix.optiflow.data.remote.dto.RegisterPatientRequest
import com.logix.optiflow.data.remote.dto.TimeSlotDto
import com.logix.optiflow.data.remote.dto.WorkOrderDto
import com.logix.optiflow.data.remote.dto.ClinicalRecordDto
import java.util.UUID
import retrofit2.http.Body
import retrofit2.http.GET
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

    @GET("patients/{patientId}")
    suspend fun getPatient(
        @Path("patientId") patientId: UUID,
    ): PatientDto

    @POST("appointments")
    suspend fun bookAppointment(
        @Body request: BookAppointmentRequest,
    ): AppointmentDto

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

    @GET("patients/{patientId}/clinical-records")
    suspend fun getPatientClinicalRecords(
        @Path("patientId") patientId: UUID,
    ): List<ClinicalRecordDto>
}
