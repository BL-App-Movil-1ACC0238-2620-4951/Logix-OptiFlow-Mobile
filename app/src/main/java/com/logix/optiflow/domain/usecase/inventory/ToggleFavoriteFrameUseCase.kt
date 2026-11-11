package com.logix.optiflow.domain.usecase.inventory

import com.logix.optiflow.domain.repository.inventory.FavoriteFrameRepository
import java.util.UUID
import javax.inject.Inject

class ToggleFavoriteFrameUseCase @Inject constructor(
    private val repository: FavoriteFrameRepository,
) {
    suspend operator fun invoke(patientId: UUID, sku: String) = repository.toggle(patientId, sku)
}
