package com.logix.optiflow.domain.usecase.searchbooking

import com.logix.optiflow.domain.repository.searchbooking.AppointmentRepository
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/** Primer turno libre (desde ahora) de cada óptica; las que no tienen turnos no aparecen. */
class GetFirstAvailableSlotsUseCase @Inject constructor(
    private val repository: AppointmentRepository,
) {
    suspend operator fun invoke(storeIds: List<UUID>): Map<UUID, Instant> =
        coroutineScope {
            val now = Instant.now()
            storeIds.distinct().map { id ->
                async {
                    val first = repository.getAvailability(id).getOrNull()
                        ?.map { it.startDateTime }
                        ?.filter { it.isAfter(now) }
                        ?.minOrNull()
                    first?.let { id to it }
                }
            }.awaitAll().filterNotNull().toMap()
        }
}
