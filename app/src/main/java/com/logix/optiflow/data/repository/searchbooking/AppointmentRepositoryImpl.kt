package com.logix.optiflow.data.repository.searchbooking

import com.logix.optiflow.data.local.PatientSessionStore
import com.logix.optiflow.data.mapper.searchbooking.toDomain
import com.logix.optiflow.data.remote.api.searchbooking.SearchBookingApi
import com.logix.optiflow.data.remote.dto.searchbooking.BookAppointmentRequest
import com.logix.optiflow.data.remote.dto.searchbooking.AppointmentDto
import com.logix.optiflow.data.remote.dto.searchbooking.PublishTimeSlotsRequest
import com.logix.optiflow.data.remote.dto.searchbooking.SlotRequest
import com.logix.optiflow.data.remote.dto.searchbooking.UpdateAppointmentStatusRequest
import com.logix.optiflow.data.remote.safeApiCall
import com.logix.optiflow.data.repository.offline.OfflineDataRepositoryImpl
import com.logix.optiflow.domain.model.searchbooking.Appointment
import com.logix.optiflow.domain.model.searchbooking.TimeSlot
import com.logix.optiflow.domain.repository.searchbooking.AppointmentRepository
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppointmentRepositoryImpl @Inject constructor(
    private val api: SearchBookingApi,
    private val sessionStore: PatientSessionStore,
    private val offline: OfflineDataRepositoryImpl,
) : AppointmentRepository {

    override suspend fun getTimeSlots(opticalStoreId: UUID): Result<List<TimeSlot>> =
        safeApiCall { api.getTimeSlots(opticalStoreId).map { it.toDomain() } }

    override suspend fun publishTimeSlots(
        opticalStoreId: UUID,
        slots: List<Pair<Instant, Instant>>,
    ): Result<List<TimeSlot>> =
        safeApiCall {
            api.publishTimeSlots(
                opticalStoreId,
                PublishTimeSlotsRequest(
                    slots.map { (start, end) -> SlotRequest(start, end) },
                ),
            ).map { it.toDomain() }
        }

    override suspend fun getAvailability(opticalStoreId: UUID): Result<List<TimeSlot>> =
        safeApiCall {
            api
                .getTimeSlots(opticalStoreId)
                .map { it.toDomain() }
                .filter { it.status.equals("AVAILABLE", ignoreCase = true) }
        }

    override suspend fun bookAppointment(
        opticalStoreId: UUID,
        timeSlotId: UUID,
        patientId: UUID?,
    ): Result<Appointment> =
        safeApiCall {
            val forPatient =
                patientId ?: sessionStore.read()?.patientId
                    ?: error("Patient session not found. Log in before booking.")
            api
                .bookAppointment(
                    BookAppointmentRequest(
                        patientId = forPatient,
                        opticalStoreId = opticalStoreId,
                        timeSlotId = timeSlotId,
                    ),
                )
                .toDomain()
        }

    override suspend fun getPatientAppointments(patientId: UUID): Result<List<Appointment>> =
        offline.list("appointments/$patientId", AppointmentDto::class.java) { api.getPatientAppointments(patientId) }
            .map { dtos -> dtos.map { it.toDomain() } }

    override suspend fun getStoreAgenda(opticalStoreId: UUID, date: LocalDate): Result<List<Appointment>> =
        safeApiCall {
            api.getStoreAgenda(opticalStoreId, date.toString()).map { it.toDomain() }
        }

    override suspend fun cancelAppointment(appointmentId: UUID): Result<Unit> =
        safeApiCall {
            api.updateAppointmentStatus(appointmentId, UpdateAppointmentStatusRequest(status = "CANCELLED"))
            Unit
        }

    override suspend fun completeAppointment(appointmentId: UUID): Result<Unit> =
        safeApiCall {
            api.updateAppointmentStatus(appointmentId, UpdateAppointmentStatusRequest(status = "COMPLETED"))
            Unit
        }
}
