package com.logix.optiflow.data.repository.searchbooking

import com.logix.optiflow.data.local.PatientSessionStore
import com.logix.optiflow.data.mapper.searchbooking.toDomain
import com.logix.optiflow.data.remote.api.searchbooking.SearchBookingApi
import com.logix.optiflow.data.remote.dto.searchbooking.BookAppointmentRequest
import com.logix.optiflow.data.remote.dto.searchbooking.UpdateAppointmentStatusRequest
import com.logix.optiflow.data.remote.safeApiCall
import com.logix.optiflow.domain.model.searchbooking.Appointment
import com.logix.optiflow.domain.model.searchbooking.TimeSlot
import com.logix.optiflow.domain.repository.searchbooking.AppointmentRepository
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppointmentRepositoryImpl @Inject constructor(
    private val api: SearchBookingApi,
    private val sessionStore: PatientSessionStore,
) : AppointmentRepository {

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
    ): Result<Appointment> =
        safeApiCall {
            val session =
                sessionStore.read()
                    ?: error("Patient session not found. Log in before booking.")
            api
                .bookAppointment(
                    BookAppointmentRequest(
                        patientId = session.patientId,
                        opticalStoreId = opticalStoreId,
                        timeSlotId = timeSlotId,
                    ),
                )
                .toDomain()
        }

    override suspend fun getPatientAppointments(patientId: UUID): Result<List<Appointment>> =
        safeApiCall {
            api.getPatientAppointments(patientId).map { it.toDomain() }
        }

    override suspend fun getStoreAgenda(opticalStoreId: UUID, date: LocalDate): Result<List<Appointment>> =
        safeApiCall {
            api.getStoreAgenda(opticalStoreId, date.toString()).map { it.toDomain() }
        }

    override suspend fun cancelAppointment(appointmentId: UUID): Result<Unit> =
        safeApiCall {
            api.updateAppointmentStatus(appointmentId, UpdateAppointmentStatusRequest(status = "CANCELLED"))
            Unit
        }
}
