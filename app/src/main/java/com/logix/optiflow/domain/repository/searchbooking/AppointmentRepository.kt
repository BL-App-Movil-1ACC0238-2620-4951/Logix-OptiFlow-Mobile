package com.logix.optiflow.domain.repository.searchbooking

import com.logix.optiflow.domain.model.searchbooking.Appointment
import com.logix.optiflow.domain.model.searchbooking.TimeSlot
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

interface AppointmentRepository {
    suspend fun getAvailability(opticalStoreId: UUID): Result<List<TimeSlot>>

    /** Todos los turnos publicados de la óptica, ocupados o libres. */
    suspend fun getTimeSlots(opticalStoreId: UUID): Result<List<TimeSlot>>

    suspend fun publishTimeSlots(opticalStoreId: UUID, slots: List<Pair<Instant, Instant>>): Result<List<TimeSlot>>

    /** Sin [patientId] reserva para el paciente de la sesión; el personal lo indica. */
    suspend fun bookAppointment(
        opticalStoreId: UUID,
        timeSlotId: UUID,
        patientId: UUID? = null,
    ): Result<Appointment>

    suspend fun getPatientAppointments(patientId: UUID): Result<List<Appointment>>

    /** Citas de una óptica en un día (agenda del personal). */
    suspend fun getStoreAgenda(opticalStoreId: UUID, date: LocalDate): Result<List<Appointment>>

    suspend fun cancelAppointment(appointmentId: UUID): Result<Unit>

    /** Marca la cita como atendida al terminar la atención clínica. */
    suspend fun completeAppointment(appointmentId: UUID): Result<Unit>
}
