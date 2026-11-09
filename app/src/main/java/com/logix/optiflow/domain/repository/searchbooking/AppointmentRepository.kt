package com.logix.optiflow.domain.repository.searchbooking

import com.logix.optiflow.domain.model.searchbooking.Appointment
import com.logix.optiflow.domain.model.searchbooking.TimeSlot
import java.time.LocalDate
import java.util.UUID

interface AppointmentRepository {
    suspend fun getAvailability(opticalStoreId: UUID): Result<List<TimeSlot>>

    suspend fun bookAppointment(
        opticalStoreId: UUID,
        timeSlotId: UUID,
    ): Result<Appointment>

    suspend fun getPatientAppointments(patientId: UUID): Result<List<Appointment>>

    /** Citas de una óptica en un día (agenda del personal). */
    suspend fun getStoreAgenda(opticalStoreId: UUID, date: LocalDate): Result<List<Appointment>>

    suspend fun cancelAppointment(appointmentId: UUID): Result<Unit>
}
