package com.logix.optiflow.domain.usecase.inventory

import com.logix.optiflow.domain.model.inventory.FrameModel
import com.logix.optiflow.domain.repository.inventory.FrameModelRepository
import javax.inject.Inject

class GetFrameCatalogUseCase @Inject constructor(
    private val repository: FrameModelRepository,
) {
    suspend operator fun invoke(): Result<List<FrameModel>> = repository.getFrameCatalog()
}
