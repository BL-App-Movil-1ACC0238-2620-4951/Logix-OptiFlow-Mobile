package com.logix.optiflow.domain.usecase.inventory

import com.logix.optiflow.domain.model.inventory.FrameStock
import com.logix.optiflow.domain.repository.inventory.FrameModelRepository
import com.logix.optiflow.domain.repository.searchbooking.OpticalStoreRepository
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/** Stock de una montura en cada óptica activa; las ópticas sin unidades no se incluyen. */
class GetFrameStockUseCase @Inject constructor(
    private val opticalStoreRepository: OpticalStoreRepository,
    private val frameModelRepository: FrameModelRepository,
) {
    suspend operator fun invoke(sku: String): Result<List<FrameStock>> =
        opticalStoreRepository.getOpticalStores().mapCatching { stores ->
            coroutineScope {
                stores
                    .map { store ->
                        async {
                            frameModelRepository.getFrame(sku, store.id).getOrNull()?.quantity
                                ?.takeIf { it > 0 }
                                ?.let { FrameStock(store, it) }
                        }
                    }.awaitAll()
                    .filterNotNull()
            }
        }
}
