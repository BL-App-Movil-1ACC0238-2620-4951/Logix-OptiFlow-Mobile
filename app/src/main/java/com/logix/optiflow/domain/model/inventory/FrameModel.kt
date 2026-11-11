package com.logix.optiflow.domain.model.inventory

import com.logix.optiflow.domain.model.searchbooking.OpticalStore
import java.util.UUID

data class FrameModel(
    val id: UUID,
    val sku: String,
    val name: String,
    val material: String?,
    val shape: String?,
    val color: String?,
    val price: Double,
    val currency: String,
    val tryOnModelUrl: String?,
    /** Solo viene cuando se consulta la montura en una óptica. */
    val quantity: Int?,
    val minimumStock: Int? = null,
)

/** Unidades de una montura en una óptica. */
data class FrameStock(
    val store: OpticalStore,
    val quantity: Int,
)
