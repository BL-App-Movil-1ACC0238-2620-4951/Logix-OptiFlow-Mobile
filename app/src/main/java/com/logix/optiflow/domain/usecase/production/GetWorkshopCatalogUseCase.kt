package com.logix.optiflow.domain.usecase.production

import com.logix.optiflow.domain.model.production.NamedRef
import com.logix.optiflow.domain.repository.production.WorkOrderRepository
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/** Técnicos y laboratorios disponibles para asignar una orden. */
data class WorkshopCatalog(
    val technicians: List<NamedRef>,
    val laboratories: List<NamedRef>,
)

class GetWorkshopCatalogUseCase @Inject constructor(
    private val repository: WorkOrderRepository,
) {
    suspend operator fun invoke(): WorkshopCatalog =
        coroutineScope {
            val technicians = async { repository.getTechnicians().getOrDefault(emptyList()) }
            val laboratories = async { repository.getLaboratories().getOrDefault(emptyList()) }
            WorkshopCatalog(technicians.await(), laboratories.await())
        }
}
