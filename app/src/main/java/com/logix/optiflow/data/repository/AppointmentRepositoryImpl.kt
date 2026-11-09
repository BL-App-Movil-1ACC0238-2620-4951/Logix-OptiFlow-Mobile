package com.logix.optiflow.data.repository

import com.logix.optiflow.data.local.PatientSessionStore
import com.logix.optiflow.data.mapper.toDomain
import com.logix.optiflow.data.remote.api.SearchBookingApi
import com.logix.optiflow.data.remote.dto.BookAppointmentRequest
import com.logix.optiflow.domain.model.Appointment
import com.logix.optiflow.domain.model.TimeSlot
import com.logix.optiflow.domain.repository.AppointmentRepository
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppointmentRepositoryImpl(
    private val api: SearchBookingApi,
    private val sessionStore: PatientSessionStore,
) : AppointmentRepository {

    override suspend fun getAvailability(opticalStoreId: UUID): List<TimeSlot> =
        withContext(Dispatchers.IO) {
            api
                .getTimeSlots(opticalStoreId)
                .map { it.toDomain() }
                .filter { it.status.equals("AVAILABLE", ignoreCase = true) }
        }

    override suspend fun bookAppointment(
        opticalStoreId: UUID,
        timeSlotId: UUID,
    ): Appointment =
        withContext(Dispatchers.IO) {
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

    override suspend fun getPatientAppointments(patientId: UUID): List<Appointment> =
        withContext(Dispatchers.IO) {
            api.getPatientAppointments(patientId).map { it.toDomain() }
        }
}
