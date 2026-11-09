package com.logix.optiflow.domain.repository.searchbooking

import com.logix.optiflow.domain.model.searchbooking.Appointment
import com.logix.optiflow.domain.model.searchbooking.TimeSlot
import java.util.UUID

interface AppointmentRepository {
    suspend fun getAvailability(opticalStoreId: UUID): Result<List<TimeSlot>>

    suspend fun bookAppointment(
        opticalStoreId: UUID,
        timeSlotId: UUID,
    ): Result<Appointment>

    suspend fun getPatientAppointments(patientId: UUID): Result<List<Appointment>>
}
