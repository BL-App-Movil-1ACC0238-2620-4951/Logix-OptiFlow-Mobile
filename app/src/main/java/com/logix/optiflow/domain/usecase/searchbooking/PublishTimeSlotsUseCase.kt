package com.logix.optiflow.domain.usecase.searchbooking

import com.logix.optiflow.domain.model.searchbooking.TimeSlot
import com.logix.optiflow.domain.repository.searchbooking.AppointmentRepository
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject

/**
 * Abre los turnos de un día: corta el rango [desde, hasta) en turnos de la duración indicada y los
 * publica juntos con `POST /optical-stores/{id}/time-slots`.
 */
class PublishTimeSlotsUseCase @Inject constructor(
    private val repository: AppointmentRepository,
) {
    suspend operator fun invoke(
        opticalStoreId: UUID,
        day: LocalDate,
        from: LocalTime,
        to: LocalTime,
        minutes: Long,
        zone: ZoneId,
    ): Result<List<TimeSlot>> {
        val slots = slotsFor(day, from, to, minutes, zone)
        if (slots.isEmpty()) return Result.failure(IllegalArgumentException("El rango no alcanza para un turno."))
        return repository.publishTimeSlots(opticalStoreId, slots)
    }

    companion object {
        fun slotsFor(day: LocalDate, from: LocalTime, to: LocalTime, minutes: Long, zone: ZoneId) =
            generateSequence(from) { it.plusMinutes(minutes) }
                .takeWhile { !it.plusMinutes(minutes).isAfter(to) && it.plusMinutes(minutes) > it }
                .map { start ->
                    val begin = day.atTime(start).atZone(zone).toInstant()
                    begin to begin.plus(Duration.ofMinutes(minutes))
                }
                .toList()
    }
}
