package com.logix.optiflow.domain.usecase.inventory

import com.logix.optiflow.domain.model.inventory.Supplier
import com.logix.optiflow.domain.repository.inventory.InventoryRepository
import javax.inject.Inject

class RegisterSupplierUseCase @Inject constructor(
    private val repository: InventoryRepository,
) {
    suspend operator fun invoke(name: String, contact: String?): Result<Supplier> = repository.registerSupplier(name, contact)
}
