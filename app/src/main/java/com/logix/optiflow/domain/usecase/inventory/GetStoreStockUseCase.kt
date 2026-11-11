package com.logix.optiflow.domain.usecase.inventory

import com.logix.optiflow.domain.repository.inventory.FrameModelRepository
import java.util.UUID
import javax.inject.Inject

/** Unidades por SKU en la óptica del asesor. */
class GetStoreStockUseCase @Inject constructor(
    private val repository: FrameModelRepository,
) {
    suspend operator fun invoke(opticalStoreId: UUID): Result<Map<String, Int>> =
        repository.getStoreStock(opticalStoreId)
}
