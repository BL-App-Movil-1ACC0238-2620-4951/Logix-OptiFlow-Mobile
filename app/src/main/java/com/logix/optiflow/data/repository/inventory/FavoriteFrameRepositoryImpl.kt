package com.logix.optiflow.data.repository.inventory

import com.logix.optiflow.data.local.inventory.FavoriteFrameStore
import com.logix.optiflow.domain.repository.inventory.FavoriteFrameRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class FavoriteFrameRepositoryImpl @Inject constructor(
    private val store: FavoriteFrameStore,
) : FavoriteFrameRepository {

    override fun favorites(patientId: UUID): Flow<Set<String>> = store.favorites(patientId)

    override suspend fun toggle(patientId: UUID, sku: String) = store.toggle(patientId, sku)
}
