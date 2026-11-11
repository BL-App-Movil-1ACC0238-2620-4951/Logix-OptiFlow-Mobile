package com.logix.optiflow.domain.usecase.inventory

import com.logix.optiflow.domain.model.inventory.Supplier
import com.logix.optiflow.domain.repository.inventory.InventoryRepository
import javax.inject.Inject

class GetSuppliersUseCase @Inject constructor(
    private val repository: InventoryRepository,
) {
    suspend operator fun invoke(): Result<List<Supplier>> = repository.getSuppliers()
}
