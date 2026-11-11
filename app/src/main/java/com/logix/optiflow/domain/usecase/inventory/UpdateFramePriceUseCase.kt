package com.logix.optiflow.domain.usecase.inventory

import com.logix.optiflow.domain.model.inventory.FrameModel
import com.logix.optiflow.domain.repository.inventory.FrameModelRepository
import javax.inject.Inject

class UpdateFramePriceUseCase @Inject constructor(
    private val repository: FrameModelRepository,
) {
    suspend operator fun invoke(sku: String, price: Double): Result<FrameModel> = repository.updatePrice(sku, price)
}
