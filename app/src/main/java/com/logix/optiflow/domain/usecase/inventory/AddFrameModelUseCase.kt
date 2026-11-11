package com.logix.optiflow.domain.usecase.inventory

import com.logix.optiflow.domain.model.inventory.FrameModel
import com.logix.optiflow.domain.repository.inventory.FrameModelRepository
import com.logix.optiflow.domain.repository.inventory.InventoryRepository
import java.util.UUID
import javax.inject.Inject

/** Unidades con las que entra una montura nueva a una óptica. */
data class InitialStock(
    val opticalStoreId: UUID,
    val supplierId: UUID,
    val quantity: Int,
    val minimumStock: Int?,
)

/**
 * Registra una montura en el catálogo (`POST /inventory/frames`) y, si se indica, la ingresa al
 * stock de la óptica con una reposición (`POST /inventory/replenishments`).
 */
class AddFrameModelUseCase @Inject constructor(
    private val frameRepository: FrameModelRepository,
    private val inventoryRepository: InventoryRepository,
) {
    suspend operator fun invoke(
        sku: String,
        name: String,
        material: String?,
        shape: String?,
        color: String?,
        price: Double,
        initialStock: InitialStock?,
    ): Result<FrameModel> {
        val frame = frameRepository.addFrame(sku, name, material, shape, color, price).getOrElse { return Result.failure(it) }
        if (initialStock != null) {
            inventoryRepository
                .replenish(initialStock.opticalStoreId, frame.sku, initialStock.supplierId, initialStock.quantity, initialStock.minimumStock)
                .onFailure { return Result.failure(InitialStockFailed(frame, it)) }
        }
        return Result.success(frame)
    }
}

/** La montura se creó, pero su stock inicial no se pudo registrar. */
class InitialStockFailed(val frame: FrameModel, cause: Throwable) : Exception(cause.message, cause)
