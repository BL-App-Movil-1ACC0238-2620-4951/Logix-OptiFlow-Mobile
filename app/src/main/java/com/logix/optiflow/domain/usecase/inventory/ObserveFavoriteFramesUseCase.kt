package com.logix.optiflow.domain.usecase.inventory

import com.logix.optiflow.domain.repository.inventory.FavoriteFrameRepository
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveFavoriteFramesUseCase @Inject constructor(
    private val repository: FavoriteFrameRepository,
) {
    operator fun invoke(patientId: UUID): Flow<Set<String>> = repository.favorites(patientId)
}
