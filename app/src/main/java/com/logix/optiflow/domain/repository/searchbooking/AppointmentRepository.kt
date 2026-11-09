package com.logix.optiflow.domain.repository.searchbooking

import com.logix.optiflow.domain.model.searchbooking.Appointment
import com.logix.optiflow.domain.model.searchbooking.TimeSlot
import java.util.UUID

interface AppointmentRepository {
    suspend fun getAvailability(opticalStoreId: UUID): List<TimeSlot>

    suspend fun bookAppointment(
        opticalStoreId: UUID,
        timeSlotId: UUID,
    ): Appointment

    suspend fun getPatientAppointments(patientId: UUID): List<Appointment>
}
