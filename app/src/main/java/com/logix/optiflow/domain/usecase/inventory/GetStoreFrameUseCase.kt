package com.logix.optiflow.domain.usecase.inventory

import com.logix.optiflow.domain.model.inventory.FrameModel
import com.logix.optiflow.domain.repository.inventory.FrameModelRepository
import java.util.UUID
import javax.inject.Inject

/** Una montura con sus unidades y su mínimo en una óptica. */
class GetStoreFrameUseCase @Inject constructor(
    private val repository: FrameModelRepository,
) {
    suspend operator fun invoke(sku: String, opticalStoreId: UUID): Result<FrameModel> = repository.getFrame(sku, opticalStoreId)
}
