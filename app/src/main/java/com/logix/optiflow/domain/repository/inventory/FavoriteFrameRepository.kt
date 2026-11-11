package com.logix.optiflow.domain.repository.inventory

import java.util.UUID
import kotlinx.coroutines.flow.Flow

interface FavoriteFrameRepository {
    fun favorites(patientId: UUID): Flow<Set<String>>

    suspend fun toggle(patientId: UUID, sku: String)
}
